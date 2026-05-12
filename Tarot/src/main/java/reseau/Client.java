package reseau;

import static common.Log.COMM;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import messages.Message;
import messages.NouveauJoueur;
import messages.ProtocoleMessageType;


/**
 * Représente un client du jeu connecté au serveur (et donc un joueur).
 * 
 * Permet l'envoi et la réception de messages sérialisés.
 */
public class Client {
	public static final String HOST_PAR_DEFAUT = "localhost";
	public static final int PORT_PAR_DEFAUT = 4545;
	
	/**
	 * worker gérant l'échange TCP d'objets sérialisés.
	 */
	private FullDuplexMsgWorker worker;
	

	
	/**
	 * Constructeur qui établit une connexion synchrone avec le
	 * serveur hébergé sur la machine host.
	 * La connexion est un appel bloquant. Il faut donc placer ce
	 * constructeur dans un service lors de son usage dans
	 * l'interface graphiqué géré par JavaFX.
	 * @param host
	 *         hostname de la machine hébergeant le serveur.
	 * @param port
	 *         port du serveur.
	 * @throws IOException
	 *         toutes les exceptions qui peuvent se produire lors de la connexion.
	 */
	public Client(final String host, final int port) throws IOException {
		COMM.trace("Client::Client: " + host + ":" + port);
		InetSocketAddress serveurAdresse = new InetSocketAddress(host, port);
		SocketChannel socketChannel = SocketChannel.open(serveurAdresse);
		worker = new FullDuplexMsgWorker(socketChannel);
	}

	/**
	 * Lecture complète d'un message arrivant du réseau.
	 * Cette méthode est bloquante. Il faut donc la placer dans
	 * un service lors de son usage dans l'interface graphique
	 * géré par JavaFX.
	 * @return
	 *       null si le distant s'est déconnecté avant la réception
	 *       complète, le message reçu sinon.  
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       la réception et désérialisation du message.
	 */
	public Message lireMessage() throws IOException {
		ReadMessageStatus status;
		do {
			status = worker.readMessage();
		} while ((status != ReadMessageStatus.ReadDataCompleted) 
				&& (status != ReadMessageStatus.ChannelClosed));
		if (status == ReadMessageStatus.ChannelClosed) {
			return null;
		}
		return (Message) worker.getData();

	}

	/**
	 * Envoi d'un message sur le réseau.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerMessage(final Message message) throws IOException {
		worker.sendMsg(0, message);
	}

	

	
	//################### Début de la section gérant la partie 
	//################### spécifique du protocole de cette application.


	/**
	 * Envoi des informations (le pseudonyme) concernant le nouveau joueur connecté.
	 * @param pseudo
	 *           le pseudonyme du nouveau joueur.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerInfoNouveauJoueur(final String pseudo) throws IOException  {
		COMM.trace("Client::envoyerInfoNouveauJoueur(" + pseudo + ")");	
		NouveauJoueur nouveauJoueur = new NouveauJoueur(pseudo);
		Message message = new Message(nouveauJoueur, ProtocoleMessageType.INFO_NOUVEAU_JOUEUR);
		envoyerMessage(message);
	}

	
	public void envoyerDebutDePartie() throws IOException {
		COMM.trace("Client::envoyerDebutDePartie()");		
		Message message = new Message(null, ProtocoleMessageType.DEBUT_PARTIE);
		envoyerMessage(message);
	}
	
	/* A ENLEVER
	public void envoyerLancerDes() throws IOException {
		COMM.trace("Client::envoyerLancerDes()");
		Message message = new Message(null, ProtocoleMessageType.DEMANDE_LANCEMENT_DES);
		envoyerMessage(message);
	}
	
	public void envoyerDeplacementPion() throws IOException {
		COMM.trace("Client::envoyerDeplacementPion()");
		Message message = new Message(null, ProtocoleMessageType.DEMANDE_DEPLACEMENT_PION);
		envoyerMessage(message);
	}
	*/

}
