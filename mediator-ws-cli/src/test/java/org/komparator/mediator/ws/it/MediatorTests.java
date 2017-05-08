package org.komparator.mediator.ws.it;


import static org.junit.Assert.assertNotNull;


import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.supplier.ws.*;


/**
 * Test suite
 */

public class MediatorTests extends BaseIT {
	@Test(expected = InvalidCartId_Exception.class)
	public void simple_failtest() throws InvalidItemId_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, EmptyCart_Exception, InvalidCreditCard_Exception{
		mediatorClient.buyCart("    ",null);	
	}



}
