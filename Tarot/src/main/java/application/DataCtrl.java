package application;

import reseau.Client;

/**
 * Classe qui contient toutes les références qui doivent être transmise d'un contrôleur à 
 * l'autre pour pouvoir changer de vue et retrouver des références initialisées par un ancien contrôleur.
 * Le load() de FXMLLoader contruit lui même les instances des contrôleurs avec un contructeur sans
 * paramêtre, il n'est donc pas possible de transmêtre des références aux contrôleurs par leur
 * constructeur. 
 * Pour résoudre ce problème, et pouvoir transmêtre une référence d'une instance déjà construite
 * nous utilisons le patron de conception du Singleton. La classe  DataCtrl cache son constucteur
 * en private, et ne peut être construite que par sa méthode classe getInstance(). Lors de
 * spn premier appel, elle construite une instance, et ensuite, tous les autres appels
 * retourne la référence sur l'instance déjà contruite. Ainsi le premier contrôleur construit
 * une instance de DataCtrl par un premier appel à getInstance(), et tous les autres
 * retrouve la référence de cette instance par des appels à getInstance().
 */
public class DataCtrl {
	/**
	 * Référence vers l'instance du Client connecté au Serveur du modèle.
	 */
	private Client connexion; 
	
	/**
	 * Nom du joueur
	 */
	private String monNom;
	
	
	/**
	 * reférence sur l'instance du DataCtrl déjà construite pour la
	 * mise en oeuvre du patron de conception du singleton
	 * (construction unique du DataCtrl au sein d'une même application,
	 * avec retour de la référence sur l'instance déjà contruite
	 * en cas de demande multiple de construction).
	 * Initialisée à null: aucune instance du DataCtrl n'a encore été construite
	 */
	private static DataCtrl INSTANCE = null;
	
	/**
	 * Construit une instance de DataCtrl uniquement lors de son premier appel.
	 * Tous les autres appels retournent la référence sur cette instance déjà construite. 
	 * @return
	 *       La référence sur DataCtrl construite par le premier appel. 
	 */
	static public synchronized DataCtrl getInstance() {
	    if (INSTANCE == null) {
	        // construction du DataCtrl, et initialisation de l'attribut
	        // static avec la référence sur ce DataCtrl.
	    	INSTANCE = new DataCtrl();
	    }
	    
	    // on retourne la référence du DataCtrl (soit celle qui vient
	    // d'être construite, soit celle qui a été sauvée lors de la
	    // construction initiale.
	    return INSTANCE;
	}
	

	/**
	 * Constructeur de la classe rendu privé pour que la classe ne puisse être
	 * contruite qu'une seule fois (par le premier appel de getInstance().
	 */
	private DataCtrl() {
		
	}

	
	public String getMonNom() {
		return monNom;
	}

	public void setMonNom(String monNom) {
		this.monNom = monNom;
	}


	public Client getConnexion() {
		return connexion;
	}


	public void setConnexion(Client connexion) {
		this.connexion = connexion;
	}

	


}
