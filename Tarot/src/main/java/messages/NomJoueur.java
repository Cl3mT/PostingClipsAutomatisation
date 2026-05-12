package messages;

import java.io.Serializable;

public class NomJoueur implements Serializable {
	private String nomJoueur;

	public NomJoueur(String nomJoueur) {
		super();
		this.nomJoueur = nomJoueur;
	}

	public String getNomJoueur() {
		return nomJoueur;
	}

	public void setNomJoueur(String nomJoueur) {
		this.nomJoueur = nomJoueur;
	}

}
