package tarot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable{

	private List<Carte> cartes;
	
	public Hand(){
		cartes = new ArrayList<>();
	}
	
	public List<Carte> getCartes() {
		return cartes;
	}

	public int size() {
		return cartes.size();
	}

	public void ajouter(Carte carte) {
		cartes.add(carte);
	}

	public void ajouter(List<Carte> liste) {
		for (Carte carte : liste) {
			ajouter(carte);
		}
	}

	public Carte retirer(Carte carte) {
		if (cartes.remove(carte)) {
			return carte;
		} else {
			return null;
		}
	}

	private boolean compareCarte(Carte carte1, Carte carte2) {
		Couleur couleur1=carte1.getCouleur();
		Couleur couleur2=carte2.getCouleur();
		
	    if(couleur1 == couleur2) {
	        return carte1.getValeur().getForce() > carte2.getValeur().getForce();
	    }
	    else if (couleur2 == Couleur.ATOUT) {
	        return false;
	    } 
	    else if (couleur2 == Couleur.CARREAU && couleur1 != Couleur.ATOUT) {
	        return false;
	    } 
	    else if (couleur2 == Couleur.TREFLE && couleur1 != Couleur.ATOUT && couleur1 != Couleur.CARREAU) {
	        return false;
	    } 
	    else if (couleur2 == Couleur.COEUR && couleur1 == Couleur.PIQUE) {
	        return false;
	    } 
	    else {
	        return true;
	    }
	}
	//Renvoie true si carte1 > carte2 (afin de les trier dans la main du joueur)

	public void trier() {
		List<Carte> cartesJoueur = this.getCartes();
	    int n = this.size();
	    boolean echange;
	    
	    for (int i = 0; i < n - 1; i++) {
	        echange = false;
	        for (int j = 0; j < n - i - 1; j++) {
	            if (compareCarte(cartesJoueur.get(j), cartesJoueur.get(j + 1))) {
	                // Échange des cartes dans la liste
	                Carte temp = cartesJoueur.get(j);
	                cartesJoueur.set(j, cartesJoueur.get(j + 1));
	                cartesJoueur.set(j + 1, temp);
	                
	                echange = true;
	            }
	        }
	        if(!echange){ // Si aucun échange, le tableau est déjà trié
	            break;
	        }
	    }
	} 

	public void clear() {
		if (cartes!=null) {
			cartes.clear();
		}	
	}

	public boolean possedeCouleur(Couleur couleurDemandee) {
		for (Carte carte : cartes) {
			if (carte.getCouleur()==couleurDemandee && !carte.estExcuse()){
				return true;
			}
		}
		return false;
	}

	public boolean possedeMeilleurAtout(Carte atoutABattre) {
		for (Carte carte : cartes) {
			if (carte.estAtout() && carte.getValeur().getForce() > atoutABattre.getValeur().getForce()) {
				return true;
			}
		}
		return false;
	}

	public void afficher() {
		for (int indiceCarte = 0; indiceCarte < cartes.size(); indiceCarte++) {
	        System.out.println(indiceCarte + " : " + cartes.get(indiceCarte).toString());
	    }
	}
	
	public double compterPoints() {
		double res = 0;
		for (Carte carte : cartes) {
			res+=carte.getPoints();
		}
		return res;
	}

}
