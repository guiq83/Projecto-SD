package org.komparator.mediator.ws;

public class MediatorApp {

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
			if(wsI.equals("1"))
				endpoint.setPrimary(true);			
			else
				endpoint.setPrimary(false);
			endpoint.setVerbose(true);
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
