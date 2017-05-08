package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();

		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", 1);
	}
		

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEndOfLineTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", 1);
	}
	
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductLessThanOneTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 0);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductNegativeTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", -1);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductMoreThanExistentTest() throws BadProductId_Exception,  BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 12);
	}
			
	
	
	@Test
	public void SucessTest1() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 5);
		ProductView productView = client.getProduct("X1");
		assertEquals(5,productView.getQuantity());
	}
	
	
	@Test
	public void SucessTest3() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 10);
		ProductView productView = client.getProduct("X1");
		assertEquals(0,productView.getQuantity());
		
	}


	
}
