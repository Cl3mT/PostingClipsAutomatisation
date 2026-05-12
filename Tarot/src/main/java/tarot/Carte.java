package tarot;

import java.io.Serializable;

public class Carte implements Serializable {
	
	private Couleur couleur;
	private Valeur valeur;
	private String fichier;
	
	public Carte(Couleur couleur,Valeur valeur){
		this.couleur=couleur;
		this.valeur=valeur;
		this.fichier = valeur.getNom() + "_" + couleur.getNom() + ".jpg";
	}
	
	public Couleur getCouleur() {
		return couleur;
	}

	public Valeur getValeur() {
		return valeur;
	}

	public double getPoints() {
		return valeur.getPoints();
	}
	
	public String getFichier() {
		return fichier;
	}

	public boolean estAtout() {
		return couleur==Couleur.ATOUT;
	}

	public boolean estBout() {
		return valeur==Valeur.EXCUSE || valeur==Valeur.PETIT || valeur==Valeur.VINGTETUN;	
	}

	public boolean estExcuse() {
		return valeur==Valeur.EXCUSE;
	}
	
	public String toString() {
		return valeur.toString() + " de " + couleur.getNom();
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
	    }
		else if (obj == null || getClass() != obj.getClass()) {
			return false;
	    }
		else {
			Carte autreCarte = (Carte) obj;
	    		
			return this.getCouleur() == autreCarte.getCouleur() && this.getValeur() == autreCarte.getValeur();
		}
	}

}
