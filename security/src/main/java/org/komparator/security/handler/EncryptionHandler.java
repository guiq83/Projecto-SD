package org.komparator.security.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPBody;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.security.Key;
import java.security.Security;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.PublicKey;


/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class EncryptionHandler implements SOAPHandler<SOAPMessageContext> {

	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		handleEncryption(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}

	private void handleEncryption(SOAPMessageContext smc) {
		
		System.out.println("EncryptionHandler: Handling message.");
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if (outbound){	//encrypt
			getContentEncrypt(smc, false);
		}
		else{				//decrypt
			getContentEncrypt(smc, true);
		}
	}
	
	private void getContentEncrypt(SOAPMessageContext context, boolean decrypt){
	   String OPERATION_NAME_TO_CIPHER = "buyCart";
	   String NAME_OF_SECRET_ARGUMENT = "creditCardNr";
	   
	   try{
	      SOAPMessageContext smc = (SOAPMessageContext) context;
         SOAPMessage msg = smc.getMessage();
         SOAPPart sp = msg.getSOAPPart();
         SOAPEnvelope se = sp.getEnvelope();
         SOAPBody sb = se.getBody();
         SOAPHeader sh = se.getHeader();
        
		   if (sh == null) { sh = se.addHeader(); }
		  
		   QName svcn = (QName) smc.get(MessageContext.WSDL_SERVICE);
		   QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);
		  
		   if (!opn.getLocalPart().equals(OPERATION_NAME_TO_CIPHER)) {return;}
			NodeList children = sb.getFirstChild().getChildNodes();
		  
		   for (int i = 0; i < children.getLength(); i++) {
		      Node argument = children.item(i);
		      if (argument.getNodeName().equals(NAME_OF_SECRET_ARGUMENT)) {
		         String secretArgument = argument.getTextContent();
		         if(decrypt){
		            argument.setTextContent( CryptoUtil.asymDecipher(secretArgument, CryptoUtil.getPrivateKey() ) );
		         }
		         else{
		            argument.setTextContent( CryptoUtil.asymCipher(secretArgument, CryptoUtil.getPublicKey()) );
		         }
		         
		         msg.saveChanges();
		      }
		   }
      
      } catch(Exception e){ System.out.println(e.toString());}
      return;
      
   }

}
