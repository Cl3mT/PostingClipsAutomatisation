package application;

import static common.Log.COMM;
import static common.Log.CTRL;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import messages.Message;
import messages.ProtocoleMessageType;
import reseau.Client;
import tarot.Carte;
import tarot.Hand;
import tarot.Pli;
import tarot.Regles;
import tarot.Contrat;

public class TarotViewCtrl implements javafx.fxml.Initializable  {
	/*=============================================
	 * Section de code générée par SceneBuilder.
	 * Si la scéne décrite est enrichie, reporter ici les
	 * nouveaux attributs qui apparaîtraient dans le code
	 * généré par SceneBuilder.
	 */
	/**
	 * container root de la scène dans lequel il faut placer
	 * l'arborescence de tous les autres widgets.
	 */
	@FXML
	private HBox hboxRoot;
	
	@FXML
	private TextArea texteConsole;
	
	@FXML
	private HBox boiteEncheres; // Fait le lien avec la boîte du FXML

	// --- Actions des 5 boutons d'enchères ---
	@FXML
	private void onBoutonPasse() { envoyerContrat(Contrat.PASSE); }
	
	@FXML
	private void onBoutonPetite() { envoyerContrat(Contrat.PETITE); }
	
	@FXML
	private void onBoutonGarde() { envoyerContrat(Contrat.GARDE); }
	
	@FXML
	private void onBoutonGardeSans() { envoyerContrat(Contrat.GARDESANS); }
	
	@FXML
	private void onBoutonGardeContre() { envoyerContrat(Contrat.GARDECONTRE); }


	/*=============================================
	 * à partir d'ici les attributs ajoutés à main.
	 */
	
	/**
	 * Canvas dans laquelle on va dessiner toute la page.
	 */
	private Canvas canvas;

	/**
	 * Contexte graphique placé dans le canvas.
	 * Il va servire à dessiner l'image de la scene.
	 */
	private GraphicsContext  gc;
	
	private Image imageDeFond;
	private Map<String, Image> cacheImagesCartes;//Cache pour éviter de recharger les cartes à chaque fois

	private ProtocoleMessageType lastMessageType;
	/**
	 * référence sur l'instance des data contruite par le premier contrôleur.
	 */
	private DataCtrl dataCtrl;

	/**
	 * référence sur la connexion (préalablement créée dans le
	 * contrôleur ConnexionCtrl).
	 */
	private Client connexion;

	/**
	 * mon nom de joueur (valeur sauvée dans
	 * dataCtrl par le contrôleur ConnexionCtrl).
	 */
	private String monNom;
	
	
	private Service<Message> receptionService;
	
	private Hand maMain;
	
	private Pli pliActuel = null;
	
	private boolean attenteCarte = false; // joueur vérifie si c'est à lui de jouer
	private boolean attenteContrat = false;
	private Contrat contratABattre = null;
	
	
	/*=============================================
	 * ci dessous la méthode initialize qui est automatiquement appelée à la fin
	 * du chargement de la vue de SceneBuiler dans la classe d'application (Main).
	 * Tous les widgets décrits dans la vue (pour cet exemple, il n'y a que
	 * le HBox hboxRoot) sont construits et utilisables.
	 * Il faut placer dans cette méthode tout ce qui doit être fait pour que
	 * le contrôleur fonctionne:
	 *  -> dessin de ce qui doit être vu
	 *  -> mise en place des callbacks pour les events gérés par le contrôleur.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
            imageDeFond = new Image(getClass().getResourceAsStream("/images/Background V2.jpg"));
        } catch (Exception e) {
            System.err.println("Attention : Impossible de charger l'image de fond.");
        }
		
		cacheImagesCartes = new HashMap<>();
		
		// utilisation de la méthode de classe pour retrouver 
		// la référence sur l'instance de DataCtrl déjà créée.
		dataCtrl = DataCtrl.getInstance();
							
		// on retrouve les informations déjà  initialisées par le contrôleur ConnexionCtr.
		connexion = dataCtrl.getConnexion();
		monNom = dataCtrl.getMonNom();
		
		//mise en place du textArea
		texteConsole.setEditable(false);
	    texteConsole.setFocusTraversable(false);
	    texteConsole.setStyle("-fx-opacity: 0.8;");
		
		// mise en place du dessin de l'interface
		initCanvas();
		
		// mise en place des callbacks
		initMousse();  // gestion de la souris
		initKeyboard(); // gestion du clavier
		
		// mise en place du service de réception des messages
		initServiceReception();
		receptionService.start();
		
		ecrireConsole("Bienvenue " + monNom + " !");
		
		dessiner(gc);		
	}

	/**
	 * mise en place du canvas pour dessiner l'image.
	 */
	private void initCanvas() {
		canvas = new Canvas(ConfigVue.LARGEUR, ConfigVue.HAUTEUR);
		// pour que les events claviers soient visibles par le canvas
		canvas.setFocusTraversable(true);
		gc = canvas.getGraphicsContext2D();
		hboxRoot.getChildren().add(canvas);
		
		canvas.widthProperty().bind(hboxRoot.widthProperty());
		canvas.heightProperty().bind(hboxRoot.heightProperty());
		
		canvas.widthProperty().addListener((obs, oldVal, newVal) -> dessiner(gc));
	    canvas.heightProperty().addListener((obs, oldVal, newVal) -> dessiner(gc));
	}

	/**
	 * mise en place du callback pour gérer la souris.
	 */
	private void initMousse() {
		// Création de EventHandler par une classe anonyme.
		EventHandler<MouseEvent> mouseHandler = new EventHandler<>() {
			// définition de la méthode manquante (handle)
			@Override
			public void handle(MouseEvent event) {
				onClic(event); // méthode écrite plus loin dans ce contrôleur

			}
		};

		// l'équivalent des lignes ci dessus avec une expression lambda 
		// (1 seule ligne de code):
		// EventHandler<MouseEvent> mouseHandler = (event) -> onClic(event); 

		// mise en place du callback pour les events de clic de la souris:
		canvas.setOnMouseClicked(mouseHandler);
	}
	
	/**
	 * mise en place du callback pour gérer le clavier.
	 */
	private void initKeyboard() {
		// Création de EventHandler par une classe anonyme.
		EventHandler<KeyEvent> keyboardHandler = new EventHandler<>() {

			// définition de la méthode manquante (handle)
			@Override
			public void handle(KeyEvent event) {
				onKey(event); // méthode écrite plus loin dans ce contrôleur
				
			}
			
		};

		// l'équivalent des lignes ci dessus avec une expression lambda 
		// (une seule ligne de code)
		// EventHandler<KeyEvent> keyboaedHandler = (event) -> onClic(event); 

		// mise en place du callback pour les events les touches du clavier
		canvas.setOnKeyPressed(keyboardHandler);
	}
	
	private void onClic(MouseEvent event) {
	    // 1. Récupérer les coordonnées du clic de la souris
	    double x = event.getX();
	    double y = event.getY();

	    // 2. (Optionnel) Vérifier quel bouton de la souris a été cliqué
	    if (event.getButton() == MouseButton.PRIMARY) {
	        System.out.println("Clic gauche détecté aux coordonnées : X=" + x + ", Y=" + y);
	        // Ajoutez ici votre logique pour le clic gauche (ex: jouer une carte de Tarot)
	        
	        if (attenteCarte) {
	        	Carte carteChoisie = determinerCarteCliquee(x, y);
	        	
	        	if (carteChoisie != null) {
	        		System.out.println("Je joue la carte : " + carteChoisie);
	        		attenteCarte = false; // On bloque immédiatement pour éviter les double-clics
	        		
	        		try {
	        			// On envoie un message au serveur avc la carte choisie
						Message msg = new Message(carteChoisie, ProtocoleMessageType.JOUER_CARTE);
						connexion.envoyerMessage(msg); 
						
					} catch (IOException e) {
						System.err.println("Erreur lors de l'envoi de la carte.");
					}
	        	}
	        	dessiner(gc);
	        }
	        
	    } else if (event.getButton() == MouseButton.SECONDARY) {
	        System.out.println("Clic droit détecté !");
	    }
	}
	
	private void onKey(KeyEvent event) {
	    // 1. Récupérer le code de la touche qui a été pressée
	    KeyCode codeTouche = event.getCode();

	    // 2. Définir des actions en fonction de la touche
	    switch (codeTouche) {
	        case UP:
	            System.out.println("Flèche Haut pressée !");
	            // Ajoutez l'action correspondante
	            break;
	        case DOWN:
	            System.out.println("Flèche Bas pressée !");
	            // Ajoutez l'action correspondante
	            break;
	        case ENTER:
	            System.out.println("Touche Entrée pressée !");
	            // Ajoutez l'action de validation
	            break;
	        case ESCAPE:
	            System.out.println("Touche Échap pressée, annulation...");
	            // Ajoutez l'action d'annulation ou de menu
	            break;
	        default:
	            // Pour voir quelle touche a été pressée si elle n'est pas dans le switch
	            System.out.println("Touche pressée : " + codeTouche.getName());
	            break;
	    }
	}
	
	// Cette méthode rassemble tout le code de dessin
	public void dessiner(GraphicsContext gc) {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		dessinerFond(gc);
		if (maMain != null) {
			dessinerMain(gc, maMain);
		}
		if (pliActuel != null) {
			dessinerPli(gc,pliActuel);
		}
		CTRL.trace("TarotViewCtrl::dessiner");
	}
	
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
						return connexion.lireMessage();
					}
				};

			}
		};

		// Gestionaire pour la réussite de la récéption d'un message .
		EventHandler<WorkerStateEvent> succeedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// la lecture d'un message s'est terminée. On le gére.


				Message message = receptionService.getValue();

				COMM.trace("ConnexionCtrl::receptionService::succeedEvent::handle: message reçu = " + message);

				gererLesMessages(message);
				
				// on laisse la gestion du message décider de relancer ou non le service de réception

			}
		};

		// Gestionaire pour l'échec de la réception d'un message
		EventHandler<WorkerStateEvent> failedEvent = new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Gestion des décos

			}
		};


		// mise en place des 2 callbacks (réussite et échec)
		receptionService.setOnSucceeded(succeedEvent);
		receptionService.setOnFailed(failedEvent);

	}

	protected void gererLesMessages(final Message message) {
		CTRL.trace("TarotViewCtrl::gestionDesMessages: " + message);
		if (message == null) {
			// TODO Gestion des déco
			return;
		}
		lastMessageType = message.getProtocoleMessage();
		switch (message.getProtocoleMessage()) {
			case NOTIFICATION_HAND: {
				gererNotifHand(message);
				break;
			}
			case DEMANDER_CARTE: {
				System.out.println("C'est à mon tour de jouer");
				attenteCarte = true; // On débloque le clic
				dessiner(gc);
				break;
			}
			case NOTIFICATION_PLI:{
				gererNotifPli(message);
				break;
			}
			case TEXTE_CONSOLE:{
				gererTexteConsole(message);
				break;
			}
			case DEMANDER_CONTRAT: { 
				System.out.println("C'est à moi de faire une enchère !");
				this.attenteContrat = true;
				this.contratABattre = (Contrat) message.getData(); 
				boiteEncheres.setVisible(true);
				break;
			}
			default:
				COMM.warn("ConnexionCtrl::gererMessage: réception d'un message non géré: " + message.getProtocoleMessage()+ ".");
				break;
		}
		receptionService.restart();
	}

	private void envoyerContrat(Contrat contratChoisi) {
        if (attenteContrat) {
        	
        	if (contratChoisi != Contrat.PASSE && contratChoisi.getCoefficient() <= contratABattre.getCoefficient()) {
                ecrireConsole("Contrat invalide : Vous devez annoncer plus haut que " + contratABattre + " ou Passer.");
                return;
            }
        
        	try {
                Message msg = new Message(contratChoisi, ProtocoleMessageType.REPONSE_CONTRAT);
                connexion.envoyerMessage(msg);
                attenteContrat = false; 
                boiteEncheres.setVisible(false);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du contrat.");
            }
        }
    }
	
	private void gererNotifHand(Message message) {
		maMain = (Hand) message.getData();
		dessiner(gc);
	}
	
	private void gererNotifPli(Message message) {
		pliActuel = (Pli) message.getData();
		dessiner(gc);
	}
	
	private void gererTexteConsole(Message message) {
		String texte = (String) message.getData();
		ecrireConsole(texte);
	}
	
	//Renvoie l'image en regardant si l'image n'est pas dasn le cache
		private Image getImageCarte(String nomFichier) {
	        // Si l'image n'est pas encore dans notre cache, on la charge depuis le disque
	        if (!cacheImagesCartes.containsKey(nomFichier)) {
	            String chemin = "/images/Cartes_tarot/" + nomFichier;
	            try {
	                Image img = new Image(getClass().getResourceAsStream(chemin));
	                cacheImagesCartes.put(nomFichier, img); // On la sauvegarde pour la prochaine fois !
	            } catch (Exception e) {
	                System.err.println("Erreur : Impossible de charger la carte -> " + chemin);
	                return null; // Retourne null si l'image n'est pas trouvée
	            }
	        }
	        // On retourne l'image qui est maintenant en mémoire
	        return cacheImagesCartes.get(nomFichier);
	    }
		
		//Dessine l'image en vérifiant qu'elle soit dans le cache avant
		public void dessinerCarte(GraphicsContext gc, String nomFichier, double x, double y) {
	        Image imageCarte = getImageCarte(nomFichier);
	        
	        if (imageCarte != null) {
	            gc.drawImage(imageCarte, x, y, Constantes.LARGEUR_CARTE,Constantes.HAUTEUR_CARTE);
	        }
	    }
		
		// Dessine toutes les cartes de la main du joueur en les centrant en bas
		public void dessinerMain(GraphicsContext gc, Hand main) {
				//Si la main est vide ou nulle, on ne fait rien
				if (main == null || main.size() == 0) {
					return; 
				}

				int nbCartes = main.size();
				
				double largeurCarte = Constantes.LARGEUR_CARTE;  
				double hauteurCarte = Constantes.HAUTEUR_CARTE; 
				
				double espaceEntreCartes = largeurCarte + 5; //On peut mettre 35 ici si on veut que les cartes se chevauchent 
				
				//Place totale que va prendre la main à l'écran
				double largeurTotaleMain = (nbCartes - 1) * espaceEntreCartes + largeurCarte;
				
				double largeurEcranReelle = gc.getCanvas().getWidth();
				double hauteurEcranReelle = gc.getCanvas().getHeight();
				
				//On calcule le X pour être centré
				double startX = (largeurEcranReelle - largeurTotaleMain) / 2;
				
				//On calcule le y pour être en bas avec une marge de 20 pixels
				double y = hauteurEcranReelle - hauteurCarte - Constantes.MARGE;
				
				// On parcourt la main et on dessine chaque carte
				for (int i = 0; i < nbCartes; i++) {
					Carte carte = main.getCartes().get(i);
					String nomFichier = carte.getFichier(); 
					
					//on décale le X
					double x = startX + (i * espaceEntreCartes);
					
					dessinerCarte(gc, nomFichier, x, y);
					
					
					//Conditionnelle qui permet de dessiner un rectangle de couleur autour d'une carte pour savoir si elle est jouable ou non
					if (attenteCarte) {
			            boolean estJouable = true;
			            
			            if (pliActuel != null) {
			                estJouable = Regles.verifierCoupValide(carte, pliActuel, main);
			            } else {
			            		estJouable = Regles.verifierCarteEcartable(carte, main);
			            }
			            
			            gc.setLineWidth(4.0);
			            
			            if (estJouable) {
			                gc.setStroke(Color.LIMEGREEN);
			            } else {
			                gc.setStroke(Color.RED);
			            }
			            
			            gc.strokeRect(x, y, largeurCarte, hauteurCarte);
			        }
				}
			}
		
		// Dessine toutes les cartes du pli actuel en les centrant au milieu du plateau
		public void dessinerPli(GraphicsContext gc, Pli pli) {
			//TODO : A refaire en mieux
			
			// Si le pli est vide ou nul, on ne fait rien
			if (pli == null || pli.getCartes().isEmpty()) {
				return; 
			}

			int nbCartes = pli.getCartes().size();
			
			double largeurCarte = Constantes.LARGEUR_CARTE;  
			double hauteurCarte = Constantes.HAUTEUR_CARTE; 
			
			// Espace entre les cartes du pli
			double espaceEntreCartes = largeurCarte + 5; 
			
			// Place totale que va prendre le pli à l'écran
			double largeurTotalePli = (nbCartes - 1) * espaceEntreCartes + largeurCarte;
			
			double largeurEcranReelle = gc.getCanvas().getWidth();
			double hauteurEcranReelle = gc.getCanvas().getHeight();
			
			// On calcule le X pour centrer le pli horizontalement
			double startX = (largeurEcranReelle - largeurTotalePli) / 2;
			
			// On calcule le Y pour centrer le pli verticalement (au milieu)
			double y = (hauteurEcranReelle - hauteurCarte) / 2;
			
			// On parcourt les cartes du pli et on dessine chaque carte
			int i = 0;
			for (Carte carte : pli.getCartes()) {
				String nomFichier = carte.getFichier(); 
				
				// On décale le X
				double x = startX + (i * espaceEntreCartes);
				
				dessinerCarte(gc, nomFichier, x, y);
				i++;
			}
		}
		
		
		private Carte determinerCarteCliquee(double clicX, double clicY) {
			if (maMain == null || maMain.size() == 0) {
				return null;
			}

			int nbCartes = maMain.size();
			double largeurCarte = Constantes.LARGEUR_CARTE;  
			double hauteurCarte = Constantes.HAUTEUR_CARTE; 
			double espaceEntreCartes = largeurCarte + 5; 
			
			double largeurTotaleMain = (nbCartes - 1) * espaceEntreCartes + largeurCarte;
			double largeurEcranReelle = canvas.getWidth();
			double hauteurEcranReelle = canvas.getHeight();
			
			double startX = (largeurEcranReelle - largeurTotaleMain) / 2;
			double yCarte = hauteurEcranReelle - hauteurCarte - Constantes.MARGE;

			// Si on a cliqué au-dessus ou en dessous des cartes, on arrête
			if (clicY < yCarte || clicY > yCarte + hauteurCarte) {
				return null;
			}

			// On cherche quelle carte correspond au X
			for (int i = 0; i < nbCartes; i++) {
				double xCarte = startX + (i * espaceEntreCartes);
				if (clicX >= xCarte && clicX <= xCarte + largeurCarte) {
					return maMain.getCartes().get(i); 
				}
			}
			return null;
		}
		
		
		public void dessinerFond(GraphicsContext gc) {
			double largeurEcranReelle = gc.getCanvas().getWidth();
	        double hauteurEcranReelle = gc.getCanvas().getHeight();
	        
			if (imageDeFond != null) {
	            // Si l'image a bien été chargée, on la dessine sur toute la taille du canvas
	            gc.drawImage(imageDeFond, 0, 0, largeurEcranReelle, hauteurEcranReelle);
	        } else {
	            // Si l'image n'est pas trouvée, on met un fond vert classique
	            gc.setFill(Color.DARKGREEN);
	            gc.fillRect(0, 0, largeurEcranReelle, hauteurEcranReelle);
	        }
		}
		
		public void ecrireConsole(String texte) {
				texteConsole.appendText(texte + "\n");
				
				// On force la console à scroller tout en bas
				texteConsole.setScrollTop(Double.MAX_VALUE);
		}
}