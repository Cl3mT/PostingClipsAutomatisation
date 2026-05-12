package tarot;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Pli implements Serializable{
	
	private Map<Carte,Joueur> cartesJouees;
	private Couleur couleurDemandee;
	private Joueur maitre;
	private Carte carteMaitre;
	
	public Pli() {
		cartesJouees= new LinkedHashMap<Carte,Joueur>();
	}
	
	public Set<Carte> getCartes() {
		return cartesJouees.keySet();
	}

	public Couleur getCouleurDemandee() {
		return couleurDemandee;
	}

	public Joueur getMaitre() {
		return maitre;
	}

	public Carte getCarteMaitre() {
		return carteMaitre;
	}
	
	public Joueur getJoueurCarte(Carte carte) {
		//Renvoie le joueur ayant joué la carte c
		//Renvoie null si la carte n'a pas été jouée dans ce pli
		return cartesJouees.get(carte);
	}
	
	public void retirerCarte(Carte carte) {
		cartesJouees.remove(carte);
	}

	public void ajouterCarte(Joueur joueur, Carte carte) {
		if (couleurDemandee==null) {
            // La première carte définit la couleur demandée (sauf si c'est l'Excuse)
            if (carte.getValeur() != Valeur.EXCUSE) {
                couleurDemandee = carte.getCouleur();
                carteMaitre = carte;
            }
        }
        cartesJouees.put(carte, joueur);
        carteMaitre = Regles.gagnante(carteMaitre,carte,couleurDemandee);
        maitre = cartesJouees.get(carteMaitre);
	}

	public double compterPoints() {
		int res=0;
		for (Carte c : getCartes()) {
			res+=c.getPoints();
		}
		return res;
	}
	
	public int compterNbBoutsPli() {
		int nbBouts=0;
		for (Carte c : getCartes()) {
			if (c.estBout()) {
				nbBouts+=1;
			}
		}
		return nbBouts;
	}
}
