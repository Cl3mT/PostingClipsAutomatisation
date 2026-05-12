package tarot;

import java.io.Serializable;

public class Regles implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9061473864574494204L;
	public static final int POINTSZEROBOUT = 56;
	public static final int POINTSUNBOUT = 51;
	public static final int POINTSDEUXBOUTS = 41;
	public static final int POINTSTROISBOUTS = 36;
	public static final int POINTSCONTRAT = 25;
	public static final int VALEUR_PETIT_AU_BOUT = 10;

	private static int pointsNbBouts(int nbBouts) {
		switch (nbBouts) {
		case 0:
			return POINTSZEROBOUT;
		case 1:
			return POINTSUNBOUT;
		case 2:
			return POINTSDEUXBOUTS;
		case 3:
			return POINTSTROISBOUTS;
		default:
			return -1;
		}
	}

	public static boolean verifierCarteEcartable(Carte carte, Hand main) {
		// On n'écarte ni roi ni bout
		if (carte.estBout() || carte.getValeur() == Valeur.ROI) {
			return false;
		} else if (carte.estAtout()) {
			for (Carte c : main.getCartes()) {
				if (!c.estBout() && c.getValeur() != Valeur.ROI && !c.estAtout()) {
					// Le joueur possède encore au moins une carte normale,
					// donc il ne peut pas écarter d'atout
					return false;
				}
			}
			// Le joueur n'a que des atouts et rois : il peut écarter l'atout
			return true;
		}
		return true;
	}

	public static Carte gagnante(Carte carte1, Carte carte2, Couleur couleurDemandee) {
		if (carte1 == null || carte1.estExcuse()) { // se produit au début du pli
			return carte2;
		}
		if (carte2 == null || carte2.estExcuse()) {
			return carte1;
		}
		if (carte1.estAtout() != carte2.estAtout()) { // S'il y a exactement un atout parmi les deux cartes
			if (carte1.estAtout()) {
				return carte1;
			} else {
				return carte2;
			}
		}
		if (carte1.getCouleur() == carte2.getCouleur()) { // Si les deux sont de la même couleur
			if (carte1.getValeur().getForce() > carte2.getValeur().getForce()) { // Si c1 est plus forte
				return carte1;
			} else {
				return carte2;
			}
		}
		if (carte1.getCouleur() == couleurDemandee) { // Pas d'atout et pas de la même couleur : la carte gagnante est
														// celle de la couleur demandée (il y en a forcément une car
														// cette méthode est toujours utilisée dans un pli avec c1 étant
														// la carte maître
			return carte1;
		} else {
			return carte2;
		}
	}

	private static boolean verifierMonteAtout(Carte carteJouee, Pli pliActuel, Hand mainDuJoueur) {
		if (mainDuJoueur.possedeMeilleurAtout(pliActuel.getCarteMaitre())) { // S'il possède un meilleur atout
			return carteJouee.getValeur().getForce() >= pliActuel.getCarteMaitre().getValeur().getForce(); // On vérifie
																											// qu'il
																											// joue une
																											// telle
																											// carte
		}
		return true; // Sinon, il n'a pas de meilleur atout donc il peut jouer l'atout qu'il veut
	}

	public static boolean verifierCoupValide(Carte carteJouee, Pli pliActuel, Hand mainDuJoueur) {
		if (pliActuel.getCouleurDemandee() == null || carteJouee.estExcuse()) { // On peut jouer ce qu'on fait si aucune
																				// couleur demandée, et on peut toujours
																				// jouer l'excuse
			return true;
		}
		if (mainDuJoueur.possedeCouleur(pliActuel.getCouleurDemandee())) { // Si le joueur possède une carte de la
																			// couleur demandée
			if (carteJouee.getCouleur() == pliActuel.getCouleurDemandee()) { // Et qu'il cherche à jouer une telle carte
				if (pliActuel.getCouleurDemandee() != Couleur.ATOUT) { // En sans atout, il peut jouer
					return true;
				} else { // Sinon, on vérifie s'il monte à l'atout !
					return verifierMonteAtout(carteJouee, pliActuel, mainDuJoueur);
				}
			} else { // Si, par contre, le joueur cherche à jouer une carte qui n'est pas de la
						// couleur demandée alors qu'il en possède une,
				return false; // On lui refuse
			}
		} else if (mainDuJoueur.possedeCouleur(Couleur.ATOUT)) { // Si le joueur n'a pas la couleur demandée mais a de
																	// l'atout
			if (!carteJouee.estAtout()) { // Et qu'il ne cherche pas à jouer à l'atout
				return false; // On lui refuse
			} else if (pliActuel.getCarteMaitre().estAtout()) { // S'il cherche à jouer à l'atout mais qu'un autre
																// joueur a déjà joué à l'atout
				return verifierMonteAtout(carteJouee, pliActuel, mainDuJoueur); // On vérifie qu'il monte bien à l'atout
			} else { // Sinon, c'est que c'est le premier joueur à couper
				return true;
			}
		} else { // Si le joueur ne possède ni la couleur demandée, ni l'atout, il joue ce qu'il
					// veut
			return true;
		}

	}

	public static int calculerScore(Contrat contrat, int nbBouts, double pointsRealises, double pointsChien,Joueur preneur,Joueur vainqueurPetitAuBout) {
	    double pointsRealisesAvecChien;
	    
	    if (contrat != Contrat.GARDECONTRE) {
	        pointsRealisesAvecChien = pointsRealises + pointsChien;
	    } else {
	        pointsRealisesAvecChien = pointsRealises;
	    }

	    int score = (int) (POINTSCONTRAT + Math.abs(pointsRealisesAvecChien - pointsNbBouts(nbBouts))) * contrat.getCoefficient();
	    int scoreFinal;
	    
	    if (pointsRealisesAvecChien >= pointsNbBouts(nbBouts)) {
	        scoreFinal = score; // Contrat gagné : le résultat de base est positif
	    } else {
	        scoreFinal = -score; // Contrat chuté : le résultat de base est négatif
	    }
	    
	    //GESTION DU PETIT AU BOUT
	    if (vainqueurPetitAuBout != null) {
	        int prime = VALEUR_PETIT_AU_BOUT * contrat.getCoefficient();
	        if (vainqueurPetitAuBout == preneur) {
	            scoreFinal += prime; // Le preneur gagne la prime
	        } else {
	            scoreFinal -= prime; // La défense gagne la prime
	        }
	    }
	    
	    return scoreFinal;
	}
}
