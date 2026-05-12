package messages;

import java.io.Serializable;


/**
 * Classe qui sert à transporter sur le réseau les messages entre le client et le serveur.
 * 
 * La classe est sérialisable pour permettre son envoi via un réseau
 */
public class Message implements Serializable{
	/**
	 * Description du type du message de protocole transporté.
	 */
	private ProtocoleMessageType protocoleMessageType;
	
	/**
	 * Référence sur l'objet transporté dans le message.
	 * Peut être null si le type de data et l'id du joueur
	 * suffisent à transporter toute l'information.
	 */
	private Serializable data;
	
	
	
	
	/**
	 * Constructeur de la classe
	 * @param data Données à transporter.
	 * @param protocoleMessageType Type du message du protocole.
	 * @param idJoueur Identifiant du joueur concerné.
	 */
	public Message(Serializable data, ProtocoleMessageType protocoleMessageType) {
		super();
		this.data = data;
		this.protocoleMessageType = protocoleMessageType;

	}
	
	/**
	 * Getter de la data
	 * @return La data
	 */
	public Serializable getData() {
		return data;
	}

	/**
	 * Getter sur le type du message du protocole.
	 * @return le type du message du protocole
	 */
	public ProtocoleMessageType getProtocoleMessage() {
		return protocoleMessageType;
	}



	/**
	 * toString de la classe.
	 * @return une description avec le type du message de protocole.
	 */
	@Override
	public String toString() {
		return "Message [ProtocoleMessageType = " + protocoleMessageType + "]";
	}
	
	
	
	

}
