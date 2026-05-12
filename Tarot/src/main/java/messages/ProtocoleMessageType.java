package messages;

import java.io.Serializable;

/**
 * Enumération des différents messages du protocole pouvant échangé entre le client et le serveur
 * 
 * La classe est sérialisable pour permettre son envoi via un réseau.
 * 
 * Ces valeurs sont placées dans l'attribut  protocoleMessageType de la
 * classe Message. En plus de cette valeur, la classe Message peut transporter
 * une éventuelle data.
 */
public enum ProtocoleMessageType implements Serializable {
	// les différents types de messages du protocole pour gérer le jeu.
	
	// phase de connexion
	INFO_NOUVEAU_JOUEUR,  // transporte NouveauJoueur  (client -> serveur pour transporter les informations sur le joueur)
	LISTE_JOUEURS_CONNECTES, // transporte ListeJoueursConnectes ( serveur -> client pour transporter la liste des joueurs connectés)
	DEBUT_PARTIE, // transporte null (client -> serveur pour signaler qu'un joueur déclenche le début de la partie)
	NOM_JOUEUR, // transporte NumeroCoureur (serveur -> client pour signaler que la partie commence et lui donner son numéro)
	
	NOTIFICATION_HAND,
	NOTIFICATION_PLI,
	TEXTE_CONSOLE,
	
	DEMANDER_CONTRAT, // transporte Contrat (serveur -> client pour demander au joueur de choisir une enchère)
	REPONSE_CONTRAT,  // transporte Contrat (client -> serveur pour envoyer l'enchère choisie par le joueur)
	
	DEMANDER_CARTE,   // transporte null (serveur -> client pour demander au joueur de jouer une carte)
	JOUER_CARTE,      // transporte Carte (client -> serveur pour envoyer la carte jouée par le joueur)
	
	
	// message généré par la classe Serveur lors de la détection d'une déconnexion.
	DECONNEXION, // transporte null
	
	;
}

/*
    Les messages émis par les clients sont transformés en MessageSigne
    par le serveur avant de les délivrer à la classe de Course pour que
    cette classe puisse vérifier que le client émetteur est bien celui
    dont on attend un message. 
 
   Protocole entre les clients et le serveur.
 
    Serveur                           Client 
(0)      <---  Connexion de chaque client
(1)      <---  INFO_NOUVEAU_JOUEUR de chaque client)
(2)      ---> LISTE_JOUEURS_CONNECTES pour chaque connexion à tous les cliennts 
(3)      <---  DEBUT_PARTIE d'un seul client

cloture du socket de connexion (aucun nouveau client ne peut se connecter)
(4)      ---> NUMERO_COUREUR une fois pour chaque client  
             
                                   changement de vue
(5)      ---> NOUVEAU_TOUR à tous les clients (le plateau indique le numéro du joueur actif)                 
(6)      <--- DEMANDE_LANCEMENT_DES du joueur actif
(7)      ---> NOTIFICATION_VALEUR_DES à tous les clients (le plateau indique le numéro du joueur actif) 
(8)      <--- DEMANDE_DEPLACEMENT_PION du joueur actif                 
(9)     ---> NOTIFICATION_DEPLACEMENT_PION à tous les clients (le plateau indique le numéro du joueur actif) 

quand tous les pions sont arrivés
(10)     ---> NOTIFICATION_FIN à tous les clients 
        
*/
