package application;

import static common.Log.COMM;
import static common.Log.GEN;
import static common.Log.CTRL;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import messages.ListeJoueursConnectes;
import messages.Message;
import messages.NomJoueur;
import reseau.Client;
import tarot.Joueur;


public class ConnexionCtrl implements javafx.fxml.Initializable {


	@FXML
	private TextField textePseudo;

	@FXML
	private TextField texteHost;

	@FXML
	private TextField textePort;

	@FXML
	private Button boutonCommencer;

	@FXML
	private Button buttonConnexion;
	

	@FXML
	private Label labelEtatConnexion;

	@FXML
	private TextArea texteListeJoueurs;



	private Service<Client> connexionService;
	private Service<Message> receptionService;

	
	/**
	 * référence sur le classe contenant les références devant être transmises
	 * d'un contrôleur à l'autre lors du changement de vue.
	 */
	private DataCtrl dataCtrl;


	/**
	 * Initialisation du contrôleur, appelée automatiquement par JavaFX après le chargement du FXML.
	 * 
	 * démarre le service de connexion.
	 * 
	 * @param location URL de localisation du fichier FXML
	 * @param resources Ressources externes
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		CTRL.trace("ConnexionCtrl::initialize: initialisation du contrôleur de connexion");
		dataCtrl = DataCtrl.getInstance();
		
		texteListeJoueurs.setEditable(false);
		texteHost.setText(Client.HOST_PAR_DEFAUT);
		textePort.setText(""+ Client.PORT_PAR_DEFAUT);
		boutonCommencer.setManaged(false);
		boutonCommencer.setVisible(false);
		initServiceConnexion();
	}


	//#################   Partie pour la gestion des connexions #################
	/**
	 * Service lançant la tache de connexion.
	 */
	private void initServiceConnexion() {
		// service de connexion
		// La méthode call sera mise dans la tâche lancée par le service lors de
		// l'appel de méthode connexionService.start() par le clic di bouton de connexion 
		connexionService = new Service<Client>(){

			@Override
			protected Task<Client> createTask() {

				// Repère 1
				// classe anonyme qui donne le code de la méthode call() pour la connexion
				return new Task<Client>(){

					@Override
					protected Client call() throws Exception {
						COMM.trace("ConnexionCtrl::connexionService::Task::call:");
						String host = texteHost.getText();
						String portStr = textePort.getText();
						int port = Integer.parseInt(portStr);
						COMM.trace("ConnexionCtrl::connexionService::Task::call: connexion à " + host + ":" + port);
						// cet appel realise une connexion TCP, qui est un appel bloquant. Il faut donc le
						// placer dans un service.
						return new Client(host, port);
					};

				};

			}
		};


		// repère 2
		// Classe anonyme qui donne le code la méthode handle() en cas de succès de la connexion
		// la méthode handle sera appelée lorsque la méthode call() du service de connexion
		// se terminera avec succes. La méthode getValue() permet de retrouver le resultat
		// du call() réussi (la référence du Client connecté au serveur)
		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					// une fois la méthode call() du service terminée avec succès,
					// la méthode getValue() permet d'obtenir son résultat.
					Client connexion = connexionService.getValue();
					
					// on sauve cette donnée pour pouvoir la transmettre au
					// contrôleur de la seconde vue.
					dataCtrl.setConnexion(connexion);
					
					// on change la vue de l'interface graphique pour prendre en
					// compte la connexion.
					// affichage du status de connexion
					labelEtatConnexion.setText("connecté");
					
					// affichage du bouton pour commencer la course
					boutonCommencer.setManaged(true);
					boutonCommencer.setVisible(true);
					boutonCommencer.setDisable(true);

					// envoi du pseudo au serveur.
					connexion.envoyerInfoNouveauJoueur(textePseudo.getText());

					// Maintenant que le client est connecté, il peut recevoir des messages
					// du serveur. On lance le service de réception des messages 
					// Il recevra la liste des joueurs connectés envoyée par le serveur
					// à chaque nouvelle connexion, ou bien l'annonce du début de la partie.
					initServiceReception();
					receptionService.start();
				} catch (IOException e) {
					// connexion déjà perdue. On retente une nouvelle connexion.
					dataCtrl.setConnexion(null);
					labelEtatConnexion.setText("connexion perdue");

					// on relance le service de connexion.
					connexionService.restart();
				}


			}
		};

		// repère 3
		// Classe anonyme qui donne le code la méthode handle() en cas d'échec de la connexion.
		// la méthode handle sera appelée lorsque la méthode call() du service de connexion
		// se terminera en échec. On pourrait utiliser la méthode getExeption() pour connaître
		// l'exception qui a provoqué cette erreur et éventuellement la traiter.
		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO lancer un popup d'erreur.
				COMM.warn("ConnexionCtrl::connexionService::failedEvent::handle: échec de la connexion");
				dataCtrl.setConnexion(null);
				labelEtatConnexion.setText("connexion échouée");
				// on relance le service de connexion pour traiter une éventuelle nouvelle
				// tentative.
				connexionService.restart();

			}
		};

		// repère 4
		// mise en place du callBack déclenché lors du succès du service de connexion.
		connexionService.setOnSucceeded(succeedEvent);

		// mise en place du callBack déclenché lors de l'échec du service de connexion.
		connexionService.setOnFailed(failedEvent);

	}

	//#################   Partie pour la gestion des réceptions #################
	/**
	 * Service lançant la tâche de réception.
	 */
	private void initServiceReception() {
		// service de réception
		receptionService = new Service<Message>(){

			@Override
			protected Task<Message> createTask() {

				return new Task<Message>(){

					@Override
					protected Message call() throws Exception {
						// cette lecture est un appel bloquant. Il faut donc le placer
						// dans un service pour éviter de figer l'interface graphique.
						return dataCtrl.getConnexion().lireMessage();
								
					}
				};

			}
		};


		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// la lecture d'un message s'est terminée. On le gére.
				Message message = receptionService.getValue();
				gererMessage(message);

			}
		};

		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				COMM.trace("ConnexionCtrl::failedEvent::handle. Echec de la réception d'un message");
				dataCtrl.setConnexion(null);
				labelEtatConnexion.setText("connexion perdue");

				// on relance le service de connexion. (????)
				//connexionService.restart();

			}
		};


		receptionService.setOnSucceeded(succeedEvent);
		receptionService.setOnFailed(failedEvent);

	}


	private void gererMessage(final Message message) {
		COMM.trace("ConnexionCtrl::gererMessage(" + message + ")");


		if (message == null) {
			COMM.trace("ConnexionCtrl::gererMessage: connexion perdue.");
			// connexion perdue. On retente une nouvelle connexion.
			dataCtrl.setConnexion(null);
			labelEtatConnexion.setText("connexion perdue");

			// on relance le service de connexion. ????
			// connexionService.restart();
			return;
		}
		switch (message.getProtocoleMessage()) {
//		case ID_RESEAU: {
//			COMM.trace("ConnexionCtrl::gererMessage: réception de monIdResea = " + message.getIdReseau() + ".");
//			connexion.setMonIdReseau(message.getIdReseau());
//			break;
//		}
		case NOM_JOUEUR: {
			CTRL.trace("ConnexionCtrl::gererMessage: réception de NOM_JOUEUR.");
			NomJoueur nomJoueur = (NomJoueur) message.getData();
			dataCtrl.setMonNom(nomJoueur.getNomJoueur());
			changerVue();
			return; // on ne relance pas le service de réception:
			// on ne veut plus recevoir de message pour ce contrôleur.
			// le controleur de la nouvelle vue prend le relais.
		}
		case LISTE_JOUEURS_CONNECTES: {
			CTRL.trace("ConnexionCtrl::gererMessage: réception de LISTE_JOUEURS_CONNECTES.");
			afficherJoueursConnectes((ListeJoueursConnectes) message.getData());
			break;
		}
		default:
			COMM.warn("ConnexionCtrl::gererMessage: réception d'un message non géré: " + message.getProtocoleMessage()+ ".");
			break;
		}

		// on relance le service de réception pour être informé de la
		// connexion des autres joueurs ou du début de partie.
		receptionService.restart();
	}

	/**
	 * Affiche la liste des joueurs connectés.
	 * @param listeJoueurs
	 *        la liste des joueurs connectés.
	 */
	private void afficherJoueursConnectes(final ListeJoueursConnectes listeJoueurs) {
		GEN.trace("ConnexionCtrl::afficherJoueursConnectes: liste = " + listeJoueurs);
		texteListeJoueurs.clear();
		for(Joueur joueur: listeJoueurs.getJoueurs()) {
			String ligne = joueur.getNom() + " connecté.\n";
			texteListeJoueurs.appendText(ligne);
		}
		
		// On active le bouton uniquement s'il y a 4 joueurs
		if (listeJoueurs.getJoueurs().size() == 4) {
			boutonCommencer.setDisable(false);
		} else {
			boutonCommencer.setDisable(true);
		}

	}

	private void changerVue() {
		// chargement de la seconde vue.
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/fxml/TarotView.fxml"));

			// on retrouve la référence sur la scène grâce à n'importe quel widget du contrôleur.
			Scene scene = boutonCommencer.getScene();
			// on change le contenu de la scène: la nouvelle vue et son contrôleur prennent le relais.
			scene.setRoot(root);
			
			Stage stage = (Stage) scene.getWindow();
			stage.setMaximized(true);
			
			String nomDuJoueur = dataCtrl.getMonNom();
	        stage.setTitle("Jeu de Tarot - Joueur : " + nomDuJoueur);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//#################   Partie pour la gestion des deux boutons de la fenêtre de connexion #################

	@FXML
	void onCommencer(MouseEvent event) {

		try {
			dataCtrl.getConnexion().envoyerDebutDePartie();
		} catch (IOException e) {
			// TODO gestion d'erreur pour la perte de connexion
		}
	}

	/**
	 * CallBack appelé lorsque l'on clique sur le bouton qui demande la connexion.
	 * Il démare le service de connexion. La plupart du temps la connexion sera
	 * très rapide, et l'utilisateur ne verra pas de temps d'attente, et se service
	 * se terminera immédiatement.
	 * @param event
	 *        l'event lié au clic de souris.
	 *         
	 */
	@FXML
	void onConnexion(MouseEvent event) {
		// lancement de la méthode call() du service de connexion dans une tache
		// (donc un nouveau thread).
		connexionService.start();
		// la mathode handle() de succeedEvent ou de faileedEvent sera
		// appelée selon la réussite ou l'échec de cette connexion.
	}



}
