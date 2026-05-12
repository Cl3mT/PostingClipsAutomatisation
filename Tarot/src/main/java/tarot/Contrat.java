package tarot;

public enum Contrat {
	PASSE("Passe",0),
	PETITE("Petite",1),
	GARDE("Garde",2),
	GARDESANS("Garde sans",4),
	GARDECONTRE("Garde contre",6);
	
	private String nom;
	private int coefficient;
	
	Contrat(String nom, int coefficient){
		this.nom=nom;
		this.coefficient=coefficient;
	}
	
	public String getNom() {
		return nom;
	}
	
	public int getCoefficient() {
		return coefficient;
	}
	
	public String toString() {
		return getNom();
	}
}
