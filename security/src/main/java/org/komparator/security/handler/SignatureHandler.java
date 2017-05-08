package org.komparator.security.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Iterator;

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
public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

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
		handleSign(smc);
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
	
	private void handleSign(SOAPMessageContext context) {
		
		System.out.println("SignatureHandler: Handling message.");
		try{
			SOAPMessageContext smc = (SOAPMessageContext) context;
		   SOAPMessage msg = smc.getMessage();
		   SOAPPart sp = msg.getSOAPPart();
		   SOAPEnvelope se = sp.getEnvelope();
		   SOAPHeader sh = se.getHeader();
		   SOAPBody sb = se.getBody();
		  	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			String out = null;
			PrivateKey signature = null;
			if (outbound){	//sign
				if (sh == null)
					sh = se.addHeader();
				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("SignatureHeader", "d", "http://demo");
				SOAPHeaderElement element = sh.addHeaderElement(name);

				// add header element value
				signature = CryptoUtil.getPrivateKeySupplier();
				element.addTextNode(signature.toString());
				out = CryptoUtil.Sign(sb.toString(), signature);
			}
			else{				//check signature
				// get first header element
				Name name = se.createName("SignatureHeader", "d", "http://demo");
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value
				String valueString = element.getValue();
				
				if(!CryptoUtil.CheckSign(sb.toString(), valueString)){//reject
					throw new Exception("Rejected signature");
				}
			}
      } catch(Exception e){ System.out.println(e.toString());}
	}
}
