package messages;

import java.io.Serializable;
import java.util.List;

import tarot.Joueur;

public class ListeJoueursConnectes implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6558356417705660765L;
	private List<Joueur> joueurs;

	public ListeJoueursConnectes(List<Joueur> joueurs) {
		super();
		this.joueurs = joueurs;
	}

	public List<Joueur> getJoueurs() {
		return joueurs;
	}

	public void setJoueurs(List<Joueur> joueurs) {
		this.joueurs = joueurs;
	}

	@Override
	public String toString() {
		String listeStr = "ListeJoueursConnectes [";
		boolean premier = true;
		for(Joueur joueur: joueurs) {
			if (!premier) {
				listeStr += ", ";
			}
			listeStr += joueur.getNom();
			premier = false;
		}
		return listeStr + "]";
	}
	
	

}
