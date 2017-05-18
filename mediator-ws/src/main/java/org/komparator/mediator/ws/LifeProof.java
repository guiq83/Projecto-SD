package org.komparator.mediator.ws;

import org.komparator.mediator.ws.cli.MediatorClient;

import java.util.TimerTask;

public class LifeProof extends TimerTask{

	private boolean primary;
	private MediatorClient client = null;
	private MediatorEndpointManager mediator = null;
	private boolean stop= false; //flag to check if the second mediator is published
	
	public LifeProof(MediatorEndpointManager med){
		primary = false;
		mediator = med;
	}
	
	public LifeProof(String wsURL){
		primary = true;
		try{
			client = new MediatorClient(wsURL);
		} catch(Exception e){System.out.println(e.toString());}
	}
	
	@Override
	public void run() {
		
		if(!stop){ //check if the secundary mediator is published if TRUE then the thread is killed
					
		
		if(primary)
			client.imAlive();
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