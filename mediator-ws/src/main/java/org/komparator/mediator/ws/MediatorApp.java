package org.komparator.mediator.ws;

import java.util.Timer;

public class MediatorApp {
	
	private static final String WSURL2 = "http://localhost:8072/mediator-ws/endpoint";
	private static final int TIMEPERIOD = 5;

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		String wsI = null;
		Timer timer = null;
		LifeProof lifeproof = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 4) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			wsI = args[3];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
			if(wsI.equals("1")){// primario
				endpoint.setPrimary(true);
				timer = new Timer(/*isDaemon*/ true);
		        // create LifeProof object
		        lifeproof = new LifeProof(WSURL2);
		        timer.schedule(lifeproof, /*delay*/ 0 * 1000, /*period*/ TIMEPERIOD * 1000);
			}
			else				// secundario
				endpoint.setPrimary(false);
			endpoint.setVerbose(true);
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
			if(timer != null){
				timer.cancel();
				timer.purge();
			}
		}

	}

}
