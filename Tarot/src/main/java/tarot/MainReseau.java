package tarot;

import static common.Log.COMM;
import static common.Log.GEN;

import org.apache.logging.log4j.Level;

import common.Log;

public class MainReseau {
	/**
	 * number of expected arguments of main method.
	 */
	private static final int NB_ARGS = 1;

	/**
	 * position of the port number in args[].
	 */
	private static  final int PORT_ARG = 0;
	
	
	/**
	 * constructeur privé pour rendre la contruction de cette classe impossible.
	 */
	private MainReseau() {
		
	}

	
	public static void main(String[] args) throws Exception {
		if (args.length != NB_ARGS) {
			System.out.println("usage: serveur port");
			return;
		}
		int port = Integer.parseInt(args[PORT_ARG]);
		
		Log.setLevel(COMM, Level.TRACE); 
		Log.setLevel(GEN, Level.TRACE); 
		
		TarotReseau tarot = new TarotReseau(port);
		tarot.lancerTarot();
		
	}
}
