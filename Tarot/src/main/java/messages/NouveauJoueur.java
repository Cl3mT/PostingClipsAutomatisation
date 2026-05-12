package messages;

import java.io.Serializable;
import java.nio.channels.SelectionKey;

/**
 * Représente un nouveau joueur entrant dans le système
 * 
 * La classe est sérialisable pour permettre son envoi via un réseau
 */
public class NouveauJoueur implements Serializable{

	/**
	 * Pseudo choisi par le joueur
	 */
	private String pseudo;

	/**
	 * Constructeur de la classe
	 * @param pseudo Pseudo choisi par le joueur
	 */
	public NouveauJoueur(String pseudo) {
		super();
		this.pseudo = pseudo;
	}

	/**
	 * Getter du pseudo
	 * @return Le pseudo
	 */
	public String getPseudo() {
		return pseudo;
	}
}

