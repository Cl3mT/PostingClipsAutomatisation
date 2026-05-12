package messages;

/**
 * Exception levée lorsque le message reçu n'est pas du type attendu
 */
public class MessageException extends Exception {
	/**
	 * Type de données attendu
	 */
	private ProtocoleMessageType attendu;
	
	/**
	 * Type de données reçu
	 */
	private ProtocoleMessageType recu;
	
	/**
	 * Constructeur de la classe
	 * @param attendu Le type de data attendu
	 * @param recu Le type de data reçu
	 */
	public MessageException(ProtocoleMessageType attendu, ProtocoleMessageType recu) {
		super();
		this.attendu = attendu;
		this.recu = recu;
	}

	/**
	 * Renvoie un message clarifiant l'exception
	 * @return Un string indiquant les types attendu et reçu
	 */
	@Override
	public String getMessage() {
		return "Type de message attendu: " + attendu + ", Type de message reçu: " + recu + ".";
	}
	
}
