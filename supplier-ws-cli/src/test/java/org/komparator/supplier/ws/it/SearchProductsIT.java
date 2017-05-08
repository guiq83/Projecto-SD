package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();
		
		ProductView product = new ProductView();
		product.setId("X1");
		product.setDesc("Basketball");
		product.setPrice(10);
		product.setQuantity(10);
		client.createProduct(product);
		
		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}
	

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
		
	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyStringTest() throws BadText_Exception {
		client.searchProducts("");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsJustSpacesTest() throws BadText_Exception {
		client.searchProducts("       ");
	}
	
	
	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}

	@Test
	public void NotExistenteProduct() throws BadText_Exception {
		List<ProductView> PVlist = client.searchProducts("testDescription");
		assertTrue(PVlist.size()==0);
	}
	
		
	@Test
	public void sucessTest1() throws BadText_Exception, BadProductId_Exception  {
		List<ProductView> PVlist = client.searchProducts("Basketball");
		ProductView newProductView = PVlist.get(0);
		assertEquals(newProductView.getId(),"X1");
		assertTrue(PVlist.size() > 0);
		
	}
	
	
	@Test
	public void sucessTest2() throws  BadText_Exception, BadProductId_Exception {
		List<ProductView> PVlist = client.searchProducts("Basketball");
		for (ProductView pv : PVlist){
			assertEquals("Basketball", pv.getDesc());
		}
	}
	
	
	@Test
	public void sucessTest3() throws BadText_Exception {
		List<ProductView> PVlist = client.searchProducts("not basquetball");
		for (ProductView pv : PVlist){
			assertTrue("Basketball"!=pv.getDesc());
		}
		
	}
	
	@Test
	public void sucessTest4() throws BadText_Exception {
		List<ProductView> Pvlist = client.searchProducts("basketball");
		for (ProductView pv : Pvlist){
			assertTrue("Basketball"!=pv.getDesc());
		}
	}
	
		
	

}
