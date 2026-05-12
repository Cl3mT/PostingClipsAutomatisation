package tarot;

public enum Couleur {
	COEUR("Coeur","♥"),
	CARREAU("Carreau","♦"),
	TREFLE("Trefle","♣"),
	PIQUE("Pique","♠"),
	ATOUT("Atout","★");
	
	private String nom;
	private String symbole;
	
	Couleur(String nom,String symbole){
		this.nom=nom;
		this.symbole=symbole;
	}
	
	public String getNom() {
		return nom;
	}
	
	public String getSymbole() {
		return symbole;
	}
	
	public String toString() {
		return getSymbole();
	}
}
