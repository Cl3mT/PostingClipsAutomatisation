package application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Classe avec quelques exemples de tests pour illustrer l'usage de JUnit
 * 
 * @author Eric Lallet.
 *
 */
public class TestInterface {
	/**
	 * réference sur la classe qui est testée (re-créée avant chaque test par
	 * la méthode setup()
	 */
	private TarotViewCtrl underTest;

	@BeforeEach
	public void setup() {
		// re-création d'une nouvelle classe pour chaque test. 
		underTest = new TarotViewCtrl();
	}
}