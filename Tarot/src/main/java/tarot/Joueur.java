package tarot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Joueur implements Serializable{

	private String nom;
	private Hand main;
	private double scoreDonne;
	private int scoreTotal;
	private List<Pli> plisGagnes;
	private Joueur detteExcuse; //A la fin de la donne, vaut null si le joueur n'a pas l'excuse et vaut le nom du joueur qui a gagné le pli où l'excuse a été jouée par this sinon
	
	public Joueur(String nom) {
		this.nom=nom;
		this.main = new Hand();
		this.scoreDonne=0;
		this.scoreTotal=0;
		this.plisGagnes=new ArrayList<>();
		this.detteExcuse=null;
	}
	
	public String getNom() {
		return nom;
	}

	public Hand getMain() {
		return main;
	}
	public String toString() {
		return nom;
	}

	public void resetPlis() {
		plisGagnes.clear();
	}

	public void ajouterCarte(Carte carte) {
		main.ajouter(carte);
	}
	
	public void ajouterCartes(List<Carte> liste) {
		main.ajouter(liste);
	}
	
	public void retirerCarte(Carte carte) {
		main.retirer(carte);
	}
	
	public void resetScoreDonne() {
		scoreDonne=0;
	}

	public double getScoreDonne() {
		return scoreDonne;
	}
	
	public int getScoreTotal() {
		return scoreTotal;
	}

	public List<Pli> getPlisGagnes() {
		return plisGagnes;
	}

	public void setNom(String nom) {
		this.nom=nom;
	}
	
	public void ajouterDetteExcuse(Joueur joueur) {
		detteExcuse=joueur;
	}
	
	public Joueur getDetteExcuse() {
		return detteExcuse;
	}

	
	
	private void updateScoreDonne() {
		resetScoreDonne();
		for (Pli pli : plisGagnes) {
			scoreDonne+=pli.compterPoints();
		}
	}
	
	public void ramasserPli(Pli pli) {
		plisGagnes.add(pli);
		updateScoreDonne();
	}

	public void afficherMain() {
		for (Carte carte : main.getCartes()) {
			System.out.println(carte);
		}
	}
	
	public int compterNbBoutsJoueur() {
		int res=0;
		for(Pli pli : plisGagnes) {
			res+=pli.compterNbBoutsPli();
		}
		return res;
	}
	
	public void ajouterScoreTotal(int score) {
		scoreTotal+=score;
	}

}
