package tarot;

public enum Valeur {
	AS("As",1,0.5),
	DEUX("2",2,0.5),
	TROIS("3",3,0.5),
	QUATRE("4",4,0.5),
	CINQ("5",5,0.5),
	SIX("6",6,0.5),
	SEPT("7",7,0.5),
	HUIT("8",8,0.5),
	NEUF("9",9,0.5),
	DIX("10",10,0.5),
	
	VALET("V",11,1.5),
	CAVALIER("C",12,2.5),
	DAME("D",13,3.5),
	ROI("R",14,4.5),
	
	PETIT("1",1,4.5),
	ONZE("11",11,0.5),
	DOUZE("12",12,0.5),
	TREIZE("13",13,0.5),
	QUATORZE("14",14,0.5),
	QUINZE("15",15,0.5),
	SEIZE("16",16,0.5),
	DIXSEPT("17",17,0.5),
	DIXHUIT("18",18,0.5),
	DIXNEUF("19",19,0.5),
	VINGT("20",20,0.5),
	VINGTETUN("21",21,4.5),
	
	EXCUSE("E",0,4.5);
	
	private String nom;
	private int force;
	private double points;
	
	Valeur(String nom,int force,double points){
		this.nom=nom;
		this.force=force;
		this.points=points;
	}
	
	public String getNom() {
		return nom;
	}
	
	public int getForce() {
		return force;
	}
	
	public double getPoints() {
		return points;
	}
	
	public String toString() {
		return nom;
	}
}
