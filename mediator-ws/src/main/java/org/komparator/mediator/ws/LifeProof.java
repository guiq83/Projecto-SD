package org.komparator.mediator.ws;

import org.komparator.mediator.ws.cli.MediatorClient;

import java.util.TimerTask;

public class LifeProof extends TimerTask{

	private MediatorClient client = null;
	
	public LifeProof(String wsURL){
		try{
			client = new MediatorClient(wsURL);
		} catch(Exception e){System.out.println(e.toString());}
	}
	
	@Override
	public void run() {
		client.imAlive();
		// TODO Auto-generated method stub
		
	}
	
}