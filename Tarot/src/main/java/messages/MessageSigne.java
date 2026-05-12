package messages;

import java.io.Serializable;
import java.nio.channels.SelectionKey;

/**
 * Classe qui délivre le message réçu du client en lui ajoutant la signature de celui-ci
 * pour identifier la provenence du message.
 */
public class MessageSigne {
	/**
	 * le message reçu par le serveur en provenence d'un client.
	 */
	private Message message;

	/**
	 * l'id du client d'où vient ce message. Cet id est le SelectionKey
	 * attaché à la connexion de ce client pour le Selector qui gère cette connexion.
	 */
	private SelectionKey idReseau;

	/**
	 * Constuction du message signé.
	 * @param message
	 *          La référence sur le message reçu d'un client.
	 * @param clientId
	 *          La référence sur le SelectionKey attaché à ce client, qui va servir de signature. 
	 */
	public MessageSigne(Message message, SelectionKey clientId) {
		super();
		this.message = message;
		this.idReseau = clientId;
	}

	/**
	 * Getter sur l'id du client expéditeur du message.
	 * @return
	 *     la référence sur le SelectionKey attaché à ce client, qui va servir de signature.
	 */
	public SelectionKey getIdReseau() {
		return idReseau;
	}

	/**
	 * Getter sur la référence de l'objet transporté dans le message.
	 * @return
	 *       la référence sur l'objet transporté dans le message.
	 */
	public Serializable getData() {
		return message.getData();
	}

	/**
	 * Getter du type de l'objet référencé dans le champ data du message.
	 * @return 
	 *       Le type de l'objet référencé dans le champ data du message?
	 */
	public ProtocoleMessageType getProtocoleMessageType() {
		return message.getProtocoleMessage();
 	}

}
