package tarot;

import static common.Log.COMM;

import java.util.ArrayList;
import java.util.List;

public abstract class Tarot {
	public static final int NBPLIS = 18;
	public static final int NBJOUEURS = 4;
	public static final int NBCARTESCHIEN = 6;

	protected List<Joueur> joueurs;
	private Paquet paquet;
	private Hand chien;
	private Contrat contrat;
	private Joueur preneur;
	private Joueur donneur;
	private Joueur vainqueurPetitAuBout;

	private Etat etat;

	private Joueur getDonneur() {
		return donneur;
	}

	protected void setDonneur(Joueur donneur) {
		this.donneur = donneur;
	}

	protected List<Joueur> getJoueurs() {
		return joueurs;
	}

	public Tarot() {
		super();
		paquet = new Paquet();

		joueurs = new ArrayList<>();

		chien = new Hand();
		
		vainqueurPetitAuBout = null;

		etat = Etat.PHASE_DE_CONNEXION;
	}

	public void lancerTarot() throws Exception {
		boolean finDePartie = false;
		int nbPlisJoues = 0;
		int indexPremierJoueur = (joueurs.indexOf(getDonneur()) + 1) % 4;

		while (!finDePartie) {
			switch (etat) {
			case PHASE_DE_CONNEXION:
				phaseDeConnexion();
				etat = Etat.DISTRIBUTION;
				break;

			case DISTRIBUTION:
				distribution(); // Le joueur après le donneur commence
				etat = Etat.ENCHERES;
				break;

			case ENCHERES:
				try {
					contrat = faireEncheres();
					etat = Etat.ECART;
				} catch (NoOneTookException e) {
					shout("Personne n'a pris, la donne est annulée.");
			        setDonneur(joueurs.get((joueurs.indexOf(getDonneur()) + 1) % NBJOUEURS));
					reset();
					etat = Etat.DISTRIBUTION;
				}
				
				break;

			case ECART:
				faireEcart();
				etat = Etat.JEUDESPLIS;
				break;

			case JEUDESPLIS:
				indexPremierJoueur = jouerUnPli(indexPremierJoueur,nbPlisJoues);
				nbPlisJoues++;
				if (nbPlisJoues < NBPLIS) {
					etat = Etat.JEUDESPLIS;
				} else {
					etat = Etat.GESTIONSCORE;
				}
				break;

			case GESTIONSCORE:
				gererScore();
				break;

			default:
				break;

			}
		}

	}

	public void reset() {
		paquet.initialiser();
		paquet.melanger();
		for (Joueur joueur : joueurs) {
			joueur.getMain().clear();
			joueur.resetPlis();
			joueur.resetScoreDonne();
			joueur.ajouterDetteExcuse(null);
		}
		vainqueurPetitAuBout = null;
	}

	private void phaseDeConnexion() throws Exception {
		Joueur nouveauJoueur;
		nouveauJoueur = attendreNouveauJoueur();
		while (nouveauJoueur != null) {
			ajouterJoueur(nouveauJoueur);
			nouveauJoueur = attendreNouveauJoueur();
		}
		notifierNumeroJoueur();
	}

	private void distribution() throws Exception {
		COMM.trace("debut de la distribution");
		for (int numPli = 0; numPli < NBPLIS / 3; numPli++) {
			for (Joueur joueur : joueurs) {
				joueur.ajouterCartes(paquet.distribuer(3));
			}
		}
		chien.ajouter(paquet.distribuer(NBCARTESCHIEN));
		if (paquet.size() != 0) {
			System.out.println("ERREUR : FAUSSE DONNE");
		}
		notifierHands();
		COMM.trace("fin de la distribution");
	}

	/**
	 * Attend l'arrivée d'un nouveau joueur.
	 * 
	 * @return La référence sur le nouveau joueur si un nouveau joueur rejoint la
	 *         partie, ou null un joueur a déclenché le début de la course.
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract Joueur attendreNouveauJoueur() throws Exception;

	/**
	 * Envoie à tous les joueurs déjà connectés la liste des joueurs connectés après
	 * l'arrivée d'un nouveau joueur. Utilisé durant la phase de connexion.
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierListeJoueur() throws Exception;

	/**
	 * Envoie à tous les joueurs leur numéro dans la liste des joueurs. Cette
	 * notification signale la fin de la phase de connexion.
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierNumeroJoueur() throws Exception;

	/**
	 * Ajoute un joueur à la partie, et notifie la nouvelle liste de joueurs à tous
	 * les joueurs connectés (y compris le nouveau venu)
	 * 
	 * @param joueur Le nouveau joueur entrant dans la partie.
	 * @throws Exception Toutes les exceptions réseaux pouvant se produire lors de
	 *                   l'envoi des messages aux joueurs.
	 */
	private void ajouterJoueur(final Joueur joueur) throws Exception {
		this.joueurs.add(joueur);
		notifierListeJoueur();
	}

	/**
	 * Envoie à tous les joueurs leur main
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierHands() throws Exception;

	/**
	 * Envoie à tous les joueurs le pli actuel
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void notifierPli(Pli pliActuel) throws Exception;

	/**
	 * Envoie un texte dans la console d'un joueur
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void whisper(Joueur joueur, String texte) throws Exception;

	/**
	 * Envoie un texte dans la console de tous les joueurs
	 * 
	 * @throws Exception Toutes les exceptions pouvant se produire.
	 */
	protected abstract void shout(String texte) throws Exception;

	private Contrat faireEncheres() throws NoOneTookException {
		Contrat contratActuel = Contrat.PASSE;
		Joueur preneur = null;

		for (Joueur joueur : joueurs) {
			Contrat reponse = demanderContrat(joueur, contratActuel);
			
			while (reponse != Contrat.PASSE && reponse.getCoefficient() <= contratActuel.getCoefficient()) {
	            try {
	                whisper(joueur, "Vous ne pouvez pas annoncer " + reponse + " sur une " + contratActuel + ".");
	            } catch (Exception e) {
	            	
	            }
	            reponse = demanderContrat(joueur, contratActuel);
	        }
			
			if (reponse != Contrat.PASSE) {
				contratActuel = reponse;
				preneur = joueur;
			}
		}

		if (preneur == null) {
			throw new NoOneTookException();
		}

		this.preneur = preneur;
		return contratActuel;
	}

	protected abstract Contrat demanderContrat(Joueur joueur, Contrat contratActuel);

	protected abstract Carte demanderCarte(Joueur joueur);

	public Carte carteAJouer(Joueur joueur, Pli pliActuel) throws Exception {
		// Doit utiliser demanderCarte() et vérifier la validité de la carte proposée
		Carte carteJouee = demanderCarte(joueur);

		while (!Regles.verifierCoupValide(carteJouee, pliActuel, joueur.getMain())) {
			whisper(joueur, "Vous ne pouvez pas jouer cette carte ! Veuillez en choisir une autre.");

			carteJouee = demanderCarte(joueur);
		}

		joueur.retirerCarte(carteJouee);
		notifierHands();
		return carteJouee;
	}

	private void faireEcart() throws Exception {
		Carte carteChoisie = null;
		if (contrat != Contrat.GARDESANS && contrat != Contrat.GARDECONTRE) {

			preneur.ajouterCartes(chien.getCartes());
			notifierHands();

			for (int numCarte = 0; numCarte < NBCARTESCHIEN; numCarte++) {
				carteChoisie = demanderCarte(preneur);

				while (!Regles.verifierCarteEcartable(carteChoisie, preneur.getMain())) {
					whisper(preneur, "Vous ne pouvez pas jouer cette carte ! Veuillez en choisir une autre.");
					carteChoisie = demanderCarte(preneur);
				}

				chien.ajouter(carteChoisie);
				preneur.retirerCarte(carteChoisie);
				notifierHands();
			}
		}
	}

	private int jouerUnPli(int indexPremierJoueur,int nbPlisJoues) throws Exception {
		/**
		 * Renvoie l'indice du gagnant du pli, i.e. le premier à jouer dans le prochain
		 * pli.
		 */
		Pli pli = new Pli();
		notifierPli(pli);

		for (int numJoueur = 0; numJoueur < NBJOUEURS; numJoueur++) {
			int indexJoueurActuel = (indexPremierJoueur + numJoueur) % 4;
			Joueur joueurActuel = joueurs.get(indexJoueurActuel);
			Carte carteJouee = carteAJouer(joueurActuel, pli);
			pli.ajouterCarte(joueurActuel, carteJouee);
			notifierPli(pli);
		}

		Joueur vainqueur = pli.getMaitre();
		
		// GESTION DU PETIT AU BOUT
		if (nbPlisJoues == NBPLIS - 1){
			for (Carte carte : pli.getCartes()) {
				if (carte.getValeur() == Valeur.PETIT) {
					vainqueurPetitAuBout = vainqueur;
					shout("Le petit a été mené au bout par : " + vainqueur.getNom() + " !");
					break;
				}
			}
		}

		// GESTION DE L'EXCUSE

		/*
		 * Si l'excuse a été jouée, on l'enlève du pli actuel pour la donner dans un pli
		 * au joueur qui l'a jouée. On donne à ce joueur une dette d'excuse, qui nous
		 * permettra de lui prendre 0.5 lors du compte des points pour les donner au
		 * vainqueur du pli.
		 */

		// On crée une copie de la liste des cartes pour pouvoir itérer en toute
		// sécurité
		List<Carte> cartesDuPli = new ArrayList<>(pli.getCartes());

		for (Carte carte : cartesDuPli) {
			if (carte.estExcuse()) {
				Joueur proprietaireExcuse = pli.getJoueurCarte(carte);
				pli.retirerCarte(carte);
				Pli pliExcuse = new Pli();
				pliExcuse.ajouterCarte(proprietaireExcuse, carte);
				proprietaireExcuse.ramasserPli(pliExcuse);
				proprietaireExcuse.ajouterDetteExcuse(vainqueur);
				break;
			}
		}

		// On peut maintenant passer au prochain pli
		vainqueur.ramasserPli(pli);
		shout(vainqueur.getNom() + " a gagné le pli !");
		return joueurs.indexOf(vainqueur);
	}

	private void gererScore() throws Exception {
		double pointsPreneur = preneur.getScoreDonne();
		
		//Gestion de l'excuse
		for (Joueur joueur : joueurs) {
			if (joueur.getDetteExcuse() != null) {
				if (joueur.getDetteExcuse() == preneur && joueur != preneur) {
					pointsPreneur += 0.5;
					shout("La défense donne 0.5 point au preneur pour l'Excuse.");
				} else if (joueur == preneur && joueur.getDetteExcuse() != preneur){
					pointsPreneur -= 0.5;
					shout("Le preneur donne 0.5 point à la défense pour l'Excuse.");
				}
			}
		}
		
		int pointsRealises = (int) pointsPreneur;
		int pointsChien = (int) chien.compterPoints();
		int nbBouts = preneur.compterNbBoutsJoueur();
		shout("Le preneur a réalisé " + pointsRealises + " points.");
		if (contrat != Contrat.GARDECONTRE) {
			shout("On y ajoute les " + pointsChien + " points du chien,");
			shout("Ce qui fait un total de " + (pointsRealises + pointsChien) + " points.");
		}
		int score = Regles.calculerScore(contrat, nbBouts, pointsRealises, pointsChien,preneur,vainqueurPetitAuBout);

		if (score >= 0) {
			shout("Il a donc rempli le contrat.");
		} else {
			shout("Il a donc chuté du contrat.");
		}

		for (Joueur joueur : joueurs) {
			if (joueur == preneur) {
				joueur.ajouterScoreTotal(score);
				whisper(joueur, "Vous marquez " + score + " points.");
				whisper(joueur, "Votre score est maintenant de " + joueur.getScoreTotal());
			} else {
				joueur.ajouterScoreTotal(-score);
				whisper(joueur, "Vous marquez " + (-score) + " points.");
				whisper(joueur, "Votre score est maintenant de " + joueur.getScoreTotal());
			}
		}

	}
}
