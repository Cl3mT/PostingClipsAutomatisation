package tarot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paquet implements Serializable{
	private List<Carte> cartes;
	
	public Paquet() {
		cartes = new ArrayList<>();
		initialiser();
		melanger();
	}
	
	public void initialiser() {
		cartes.clear();
		
		for (Couleur couleur : Couleur.values()) { //on crée les 4 couleurs
			if (couleur != Couleur.ATOUT) {
				cartes.add(new Carte(couleur, Valeur.AS));
	            cartes.add(new Carte(couleur, Valeur.DEUX));
	            cartes.add(new Carte(couleur, Valeur.TROIS));
	            cartes.add(new Carte(couleur, Valeur.QUATRE));
	            cartes.add(new Carte(couleur, Valeur.CINQ));
	            cartes.add(new Carte(couleur, Valeur.SIX));
	            cartes.add(new Carte(couleur, Valeur.SEPT));
	            cartes.add(new Carte(couleur, Valeur.HUIT));
	            cartes.add(new Carte(couleur, Valeur.NEUF));
	            cartes.add(new Carte(couleur, Valeur.DIX));

	            cartes.add(new Carte(couleur, Valeur.VALET));
	            cartes.add(new Carte(couleur, Valeur.CAVALIER));
	            cartes.add(new Carte(couleur, Valeur.DAME));
	            cartes.add(new Carte(couleur, Valeur.ROI));
			}
		}
		
		cartes.add(new Carte(Couleur.ATOUT, Valeur.PETIT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DEUX));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.TROIS));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.QUATRE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.CINQ));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.SIX));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.SEPT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.HUIT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.NEUF));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DIX));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.ONZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DOUZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.TREIZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.QUATORZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.QUINZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.SEIZE));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DIXSEPT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DIXHUIT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.DIXNEUF));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.VINGT));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.VINGTETUN));
        cartes.add(new Carte(Couleur.ATOUT, Valeur.EXCUSE));
	
	}
	
	public int size() {
		return cartes.size();
	}
	
	public void melanger() {
		Collections.shuffle(cartes);
	}
	
	public List<Carte> distribuer(int nombreDeCartes) {
        List<Carte> aDistribuer = new ArrayList<>();
        
        for (int numCarte = 0; numCarte < nombreDeCartes; numCarte++) {
            if (!cartes.isEmpty()) {
                // On retire toujours la carte du haut (index 0)
            	aDistribuer.add(cartes.remove(0));
            } else {
                System.out.println("Erreur : Plus de cartes dans le paquet !");
                break;
            }
        }
        return aDistribuer;
    }
	
	public String toString() {
		String res = "";
		for (Carte c : cartes) {
			res+=c.toString()+"\n";
		}
		return res;
	}
}
