package reseau;



import static common.Log.COMM;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import messages.Message;
import messages.MessageSigne;
import messages.NouveauJoueur;
import messages.ProtocoleMessageType;

/**
 * Représente le serveur du jeu, utilisant NIO pour gérer les connexions clients de manière asynchrone
 * 
 * Le serveur 
 *      écoute sur le port passé en argument du constructeur, 
 *      accepte les connexions entrantes,
 *      gère les échanges de messages avec chaque client connecté
 */
public class Serveur {
	/**
	 * selector pour gérer en asynchrone tous les clients connectés.
	 */
	private Selector selector;
	/**
	 * Canal serveur pour accepter les connexions des clients (géré en asynchrone).
	 */
	private ServerSocketChannel serverSocketChannel;
	
	/**
	 * Clef enregistrée par le selector pour le serveurChannel.
	 */
	private SelectionKey serveurChannelKey;;
	/**
	 * Table d'association pour retrouver le worker en fonction de la
	 * clef retournée par le selector lors de l'arrivée d'un message 
	 * (ou de la deconnexion) d'un client.
	 */
	private Map<SelectionKey, FullDuplexMsgWorker> workersFromKey;
	
	/**
	 *  Compteur du nombre de joueurs pour bloquer à 4
	 */
	private int nombreJoueursConnectes = 0;
	

	/**
	 * Constructeur qui ouvre le serveur en mode asynchrone et initialise
	 * le selector pour la future gestion des flux entrants; ce constructeur
	 * initialise aussi la tables d'association workersFromKey
	 * qui permettra de retrouver le Worker associé à chaque client connecté.
	 * @throws IOException
	 *          toutes les exceptions qui peuvent se produire lors
	 *          de l'ouverture du selector ou du canal de connexion.
	 */
	public Serveur(int port) throws IOException {
		COMM.trace("Serveur::Serveur(" + port + ")");
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		serveurChannelKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		workersFromKey = new HashMap<>();
	}


	/**
	 * Retourne le premiere message, reçu par le serveur.
	 * Ce message peut être:
	 *   - le message de détection de la deconnexion d'un client;
	 *   - un message reçu d'un client enrichi de la signature de 
	 *     ce client pour authentifier sa provenance.
	 *   
	 * @return
	 *    Un message signé contenant le message reçu du
	 *    client, et la clef authentifiant le client
	 *    émetteur.
	 * @throws IOException
	 *    Toutes les exceptions d'entrées-sorties qui peuvent se 
	 *    produire lors de cette réception.
	 */
	public MessageSigne lireMessage() throws IOException {
		FullDuplexMsgWorker worker;
		SocketChannel rwChan;

		while (true) {
			selector.select();
			Set<SelectionKey> readyKeys = selector.selectedKeys();

			for (SelectionKey curKey : readyKeys) {
				if (curKey.isAcceptable()) {
					// connexion d'un nouveau joueur.
					SelectionKey newKey;
					rwChan = serverSocketChannel.accept();
					
					if (rwChan != null) {
						rwChan.configureBlocking(false);

						newKey = rwChan.register(selector, SelectionKey.OP_READ);
						worker = new FullDuplexMsgWorker(rwChan);
						workersFromKey.put(newKey, worker); // ajouts de cette nouvelle connexion à la Map 
						nombreJoueursConnectes++;
						if (nombreJoueursConnectes == 4) {
							COMM.info("Salon plein (4 joueurs) => fermeture des admissions.");
							cloreServerSocketChannel(); // On clot les connexions
						}
					}
				} else { // cas des keys readable
					worker = workersFromKey.get(curKey);
					ReadMessageStatus status;
					status = worker.readMessage();
					if (status == ReadMessageStatus.ChannelClosed) { // closed channel
						// déconnexion d'un joueur
						COMM.info("Server: closed channel detected");

						workersFromKey.remove(curKey);
						curKey.cancel();
						worker.close();
						nombreJoueursConnectes--;
		
						Message message = new Message(null, ProtocoleMessageType.DECONNEXION);
						MessageSigne messageSigne = new MessageSigne(message, curKey);
						return messageSigne;
					}
					if (status == ReadMessageStatus.ReadDataCompleted) {
						// réception d'une réponse d'un client.
						COMM.info("Server: reponse reçue du client");
						Message message = (Message) worker.getData();
						COMM.info("Serveur::getMessage: type du message reçu: " + message.getProtocoleMessage());
						
						MessageSigne messageSigne = new MessageSigne(message, curKey);
						return messageSigne;
					}
				}
			}
			readyKeys.clear();
		}
	}	
	
	/**
	 * fermeture du ServerSocketChannel pour interdire toute nouvelle connexion.
	 * @throws IOException 
	 */
	public void cloreServerSocketChannel() throws IOException {
		// arrêt de la surveillance par le selector.
		this.serveurChannelKey.cancel();
		
		// fermeture un canal.^
		serverSocketChannel.close();
		
	}

	/**
	 * Envoi d'un message au client identifié par key.
	 * @param key
	 *        SelectionKey associé au client destinataire du message.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message, ou suite à l'usage d'une
	 *       clef (paramètre key) ne correspondant à aucun client connecté.
	 */
	public void envoyerMessage(SelectionKey key, Message message) throws IOException {
		COMM.info("Envoi du message " + message + " à " + key);
		FullDuplexMsgWorker worker = workersFromKey.get(key);
		if (worker == null) {
			throw new IOException("Le client identifié par " + key + "n'existe pas ou n'est plus connecté");
		}
		worker.sendMsg(0, message);
	}
	
	/**
	 * Envoi un message à tous les clients connectés.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerATous(Message message) throws IOException {
		COMM.info("Envoi du message " + message + " à tous les clients");
		for (SelectionKey key: workersFromKey.keySet()) {
			envoyerMessage(key, message);
		}
	}
	/**
	 * deconnecte un client.
	 * @param key
	 *      clé du client à deconnecter.
	 * @throws IOException 
	 *      toutes les exceptions pouvant se produire durant
	 *      la fermeture du worker.
	 */
//	public void deconnecterClient(SelectionKey key) throws IOException {
//		COMM.info("Déconnexion du client " + System.identityHashCode(key));
//		FullDuplexMsgWorker worker = workersFromKey.get(key);
//		workersFromKey.remove(key);
//		
//		worker.close();
//	}
}
