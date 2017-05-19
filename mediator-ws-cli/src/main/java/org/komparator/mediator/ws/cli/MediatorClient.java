package org.komparator.mediator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import org.komparator.mediator.ws.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/**
 * Client.
 *
 * Adds easier endpoint address configuration and 
 * UDDI lookup capability to the PortType generated by wsimport.
 */
public class MediatorClient 
   implements MediatorPortType {
	
	private static final int CONNECTION_TIMEOUT = 5;
	private static final int RECEIVE_TIMEOUT = 5;
	private static final int WAIT_TIME = 10; //time to wait before retrying to connect after server is down

 /** WS service */
    MediatorService service = null;

     /** WS port (port type is the interface, port is the implementation) */
     MediatorPortType port = null;

    /** UDDI server URL */
    private String uddiURL = null;

    /** WS name */
    private String wsName = null;

    /** WS endpoint address */
    private String wsURL = null; // default value is defined inside WSDL

    public String getWsURL() {
        return wsURL;
    }

    /** output option **/
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided web service URL */
    public MediatorClient(String wsURL) throws MediatorClientException {
        this.wsURL = wsURL;
        createStub();
    }

    /** constructor with provided UDDI location and name */
    public MediatorClient(String uddiURL, String wsName) throws MediatorClientException {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        uddiLookup();
        createStub();
    }

    /** UDDI lookup */
    private void uddiLookup() throws MediatorClientException {
        try {
            if (verbose)
                System.out.printf("Contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            if (verbose)
                System.out.printf("Looking for '%s'%n", wsName);
            wsURL = uddiNaming.lookup(wsName);

        } catch (Exception e) {
            String msg = String.format("Client failed lookup on UDDI at %s!",
                    uddiURL);
            throw new MediatorClientException(msg, e);
        }

        if (wsURL == null) {
            String msg = String.format(
                    "Service with name %s not found on UDDI at %s", wsName,
                    uddiURL);
            throw new MediatorClientException(msg);
        }
    }

    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
            System.out.println("Creating stub ...");
         service = new MediatorService();
         port = service.getMediatorPort();

        if (wsURL != null) {
            if (verbose)
                System.out.println("Setting endpoint address ...");
            BindingProvider bindingProvider = (BindingProvider) port;
            Map<String, Object> requestContext = bindingProvider.getRequestContext();
            requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
            
            int connectionTimeout = CONNECTION_TIMEOUT * 1000;
            // The connection timeout property has different names in different versions of JAX-WS
            // Set them all to avoid compatibility issues

            final List<String> CONN_TIME_PROPS = new ArrayList<String>();
            CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
            CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
            CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
            // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
            for (String propName : CONN_TIME_PROPS)
                requestContext.put(propName, connectionTimeout);
            System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

            int receiveTimeout = RECEIVE_TIMEOUT * 1000;
            // The receive timeout property has alternative names
            // Again, set them all to avoid compatibility issues
            final List<String> RECV_TIME_PROPS = new ArrayList<String>();
            RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
            RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
            RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
            // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
            for (String propName : RECV_TIME_PROPS)
                requestContext.put(propName, receiveTimeout);
            System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
        }
    }

    private void retryConnection() {
    	try{
    		TimeUnit.SECONDS.sleep(WAIT_TIME); //wait for secondary server to detect primary is down and register to UDDI
    		uddiLookup();
    		createStub();
    	} catch(Exception e){System.out.println(e.toString());}
    	
    }

    // remote invocation methods ----------------------------------------------
    
    
    @Override
	 public void clear() {
    	try{
    		port.clear();
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
				port.clear();
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
	 }

    @Override
	 public String ping(String arg0) {
    	try{
    		return port.ping(arg0);
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
	    		return port.ping(arg0);
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
    	return null;
	 }
    
    @Override
	 public void imAlive() {
    	System.out.println("Primary Mediator: sent imAlive.");
		port.imAlive();
	 }
    
    @Override
	 public void updateShopHistory(ShoppingResultView shopResult){
    	try{
    		port.updateShopHistory(shopResult);
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
				port.updateShopHistory(shopResult);
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
	 }
    
    @Override
	 public void updateCart(String cartId, CartView carts){
    	try{
    		port.updateCart(cartId, carts);
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
				port.updateCart(cartId, carts);
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
	 }


    @Override
	 public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
    	try{
    		return port.searchItems(descText);
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
				return port.searchItems(descText);
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
		return null;
	 }

    @Override
	 public List<CartView> listCarts() {
    	try{
    		return port.listCarts();
		} catch(Exception e){
			System.out.println(e.toString());
			retryConnection();
			try{
				return port.listCarts();
	    	} catch(Exception e2){System.out.println(e.toString());}
		}
		return null;
	 }

	 @Override
	 public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		 try{
				return port.getItems(productId);
			} catch(Exception e){
				System.out.println(e.toString());
				retryConnection();
				try{
					return port.getItems(productId);
		    	} catch(Exception e2){System.out.println(e.toString());}
			}
			return null;
	 }

	 @Override
	 public ShoppingResultView buyCart(String cartId, String creditCardNr)
	  throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		 try{
			 return port.buyCart(cartId, creditCardNr);
			} catch(EmptyCart_Exception empty){throw empty;}
		 	catch(InvalidCartId_Exception ici){throw ici;}
		 	catch(InvalidCreditCard_Exception icc){throw icc;}
		 	catch(Exception e){
				System.out.println(e.toString());
				retryConnection();
				try{
					 return port.buyCart(cartId, creditCardNr);
		    	} catch(Exception e2){System.out.println(e.toString());}
			}
			return null;
	 }

	 @Override
	 public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		 try{
				port.addToCart(cartId, itemId, itemQty);
			} catch(InvalidCartId_Exception ici){throw ici;}
		 	catch(InvalidItemId_Exception iii){throw iii;}
		 	catch(InvalidQuantity_Exception iq){throw iq;}
		 	catch(NotEnoughItems_Exception nei){throw nei;}
		 	catch(Exception e){
				System.out.println(e.toString());
				retryConnection();
				try{
					port.addToCart(cartId, itemId, itemQty);
		    	} catch(Exception e2){System.out.println(e.toString());}
			}
	 }

	 @Override
	 public List<ShoppingResultView> shopHistory() {
		 try{
				return port.shopHistory();
			} catch(Exception e){
				System.out.println(e.toString());
				retryConnection();
				try{
					return port.shopHistory();
		    	} catch(Exception e2){System.out.println(e.toString());}
			}
			return null;
	 }
 
}