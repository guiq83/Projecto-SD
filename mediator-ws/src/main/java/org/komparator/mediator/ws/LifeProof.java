package org.komparator.mediator.ws;

import java.util.TimerTask;

public class LifeProof extends TimerTask{

	private boolean primary;
	private MediatorEndpointManager mediator = null;
	private boolean stop = false; //flag to check if the second mediator is published
	
	public LifeProof(MediatorEndpointManager med, boolean primar){
		primary = primar;
		mediator = med;
	}
	
	@Override
	public void run() {
		if(!stop){ //check if the secondary mediator is published if TRUE then the thread doesnt do anything
			if(primary)
				mediator.getClient().imAlive();
			else
				if(!mediator.checkLastDate()){
					try{
						System.out.println("Running as primary Mediator.");
						mediator.publishToUDDI();
					} catch(Exception e){System.out.println(e.toString());}
					stop = true;
				}
		}
	}
	
}