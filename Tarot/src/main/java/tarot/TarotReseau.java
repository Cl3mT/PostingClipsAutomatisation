package tarot;

import static common.Log.COMM;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;

import messages.ListeJoueursConnectes;
import messages.Message;
import messages.MessageSigne;
import messages.NouveauJoueur;
import messages.NomJoueur;
import messages.ProtocoleMessageType;

import java.util.HashMap;
import java.util.List;

import reseau.Serveur;

public class TarotReseau extends Tarot {

	private Map<Joueur,SelectionKey> joueursIdReseau;
	private Serveur serveur;
	
	public TarotReseau(final int port) throws IOException {
		super();
		this.serveur = new Serveur(port);
		joueursIdReseau = new HashMap<Joueur,SelectionKey>();
	}
	
	protected Joueur attendreNouveauJoueur() throws Exception {
		while (true) {
			MessageSigne messageSigne = serveur.lireMessage();
			
			// premier message d'un nouveau joueur connecté.
			if (messageSigne.getProtocoleMessageType() == ProtocoleMessageType.INFO_NOUVEAU_JOUEUR) {
				// On empêche plus de 4 joueurs de rejoindre
				if (joueursIdReseau.size() >= 4) {
					COMM.warn("Connexion refusée : il y a déjà 4 joueurs.");
					continue;
				}
				
				NouveauJoueur nouveau = (NouveauJoueur) messageSigne.getData();
				Joueur joueur = new Joueur(nouveau.getPseudo());
				joueursIdReseau.put(joueur, messageSigne.getIdReseau());
				return joueur;
			}
			
			// message de début de partie mettant fin à la phase de connexion.
			if (messageSigne.getProtocoleMessageType() == ProtocoleMessageType.DEBUT_PARTIE) {			
				// On empêche de lancer si on n'est pas exactement 4
				if (joueursIdReseau.size() != 4) {
					COMM.warn("Lancement refusé : il faut exactement 4 joueurs.");
					continue;
				}
				return null;
			}
			
			throw new InvalidActionException("Un message non géré est arrivé durant la mise en place des joueurs");
		}
	}

	@Override
	protected void notifierListeJoueur() throws Exception {
		ListeJoueursConnectes listeJoueurs = new ListeJoueursConnectes(joueurs);
		COMM.trace("CourseReseau::notifierListeCoureur: " + listeJoueurs);
		
		Message message = new Message(listeJoueurs, ProtocoleMessageType.LISTE_JOUEURS_CONNECTES);
		serveur.envoyerATous(message);
	}

	@Override
	protected void notifierNumeroJoueur() throws Exception {
		// on interdit les nouvelles connexions.
		serveur.cloreServerSocketChannel();
				
		List<Joueur> joueurs = getJoueurs();;
				
		// on notifie tous les clients pour qu'ils changent de vue et
		// leur donner leur nom.
		for (Joueur joueur : joueurs) {
			NomJoueur nomJoueur = new NomJoueur(joueur.getNom());
			Message message = new Message(nomJoueur, ProtocoleMessageType.NOM_JOUEUR);
			SelectionKey idReseau = joueursIdReseau.get(joueur);
			serveur.envoyerMessage(idReseau, message);
		}
	}

	@Override
	protected void notifierHands() throws Exception {
		for (Joueur joueur : joueurs) {
			Hand hand = joueur.getMain();
			hand.trier();
			Message message = new Message(hand,ProtocoleMessageType.NOTIFICATION_HAND);
			serveur.envoyerMessage(joueursIdReseau.get(joueur), message);
		}
	}
	
	@Override
	protected void notifierPli(Pli pliActuel) throws Exception {
		Message message = new Message(pliActuel,ProtocoleMessageType.NOTIFICATION_PLI);
		serveur.envoyerATous(message);
	}
	
	@Override
	protected void whisper(Joueur joueur,String texte) throws Exception {
		Message message = new Message(texte,ProtocoleMessageType.TEXTE_CONSOLE);
		serveur.envoyerMessage(joueursIdReseau.get(joueur), message);
	}
	
	@Override
	protected void shout(String texte) throws Exception{
		Message message = new Message(texte,ProtocoleMessageType.TEXTE_CONSOLE);
		serveur.envoyerATous(message);
	}
	
	@Override
	protected Carte demanderCarte(Joueur joueur) {
		try {
			// Trouver le canal de communication
			SelectionKey idReseau = joueursIdReseau.get(joueur);
			
			// Message de demande de carte
			Message demande = new Message(null, ProtocoleMessageType.DEMANDER_CARTE);
			serveur.envoyerMessage(idReseau, demande);
			
			COMM.trace("Serveur en attente de la carte de " + joueur.getNom());
			shout("C'est à " + joueur.getNom() + " de jouer.");
			whisper(joueur,"C'est à vous de jouer !");

			// Boucle d'attente de réponse
			while (true) {
				MessageSigne reponse = serveur.lireMessage(); 
				
				// Est-ce que le message vient bien du joueur à qui on a demandé ET est-ce que c'est bien l'action de jouer une carte 
				if (reponse.getIdReseau().equals(idReseau) && 
					reponse.getProtocoleMessageType() == ProtocoleMessageType.JOUER_CARTE) {
					
					Carte carteJouee = (Carte) reponse.getData();
					COMM.trace("Le serveur a reçu la carte : " + carteJouee.toString() + " de " + joueur.getNom());
					return carteJouee; 
					
				} else {
					COMM.warn("Message inattendu ignoré en attendant la carte : " + reponse.getProtocoleMessageType());
				}
			}
		} catch (Exception e) {
			COMM.error("Erreur réseau lors de la demande de carte à " + joueur.getNom());
			return null; 
		}
	}
	
	protected Contrat demanderContrat(Joueur joueur, Contrat contratActuel) {
		try {
			// On trouve le canal du joueur
			SelectionKey idReseau = joueursIdReseau.get(joueur);
			
			// On envoie l'ordre ET on glisse le contrat actuel dans le colis
			Message demande = new Message(contratActuel, ProtocoleMessageType.DEMANDER_CONTRAT);
			serveur.envoyerMessage(idReseau, demande);
			
			COMM.trace("Serveur en attente d'un contrat de " + joueur.getNom() + " (Contrat actuel: " + contratActuel + ")");

			// Boucle d'attente réseau
			while (true) {
				MessageSigne reponse = serveur.lireMessage(); 
				
				// On vérifie que c'est le bon joueur et le bon type de réponse
				if (reponse.getIdReseau().equals(idReseau) && 
					reponse.getProtocoleMessageType() == ProtocoleMessageType.REPONSE_CONTRAT) {
					
					// On déballe le contrat choisi
					Contrat contratChoisi = (Contrat) reponse.getData();
					COMM.trace(joueur.getNom() + " a annoncé : " + contratChoisi);
					
					return contratChoisi; 
					
				} else {
					COMM.warn("Message inattendu ignoré pendant les enchères : " + reponse.getProtocoleMessageType());
				}
			}
		} catch (Exception e) {
			COMM.error("Erreur réseau lors de la demande de contrat à " + joueur.getNom());
			return Contrat.PASSE; // Par sécurité en cas de déco, le joueur passe.
		}
	}

	
}


