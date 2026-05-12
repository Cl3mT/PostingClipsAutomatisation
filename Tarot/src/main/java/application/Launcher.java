package application;

/**
 * Classe Laucher pour cacher à la JVM utilisé dans les scripts que l'on
 * utilise les modules JavaFX. 
 * Cette classe ne sert que pour les scripts pour pouvoir lancer le client
 * sans passer par l'IDE.
 */
public class Launcher {
    public static void main(String[] args) {
        // On appelle la vraie classe Main ici
        Main.main(args);
    }
}