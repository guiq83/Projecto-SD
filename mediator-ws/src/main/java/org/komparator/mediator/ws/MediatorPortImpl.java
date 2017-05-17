package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;
import javax.jws.HandlerChain;

import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;


import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;




@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.wsdl",
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)

@HandlerChain(file = "/mediator-ws_handler-chain.xml")

public class MediatorPortImpl  implements MediatorPortType{
   private HashMap<String,SupplierClient> _clients = new HashMap<String,SupplierClient>(); 
   private HashMap<String,CartView> _carts = new HashMap<String,CartView>();
      
   private List<ShoppingResultView> _purchaseViews = new ArrayList<ShoppingResultView>();
   
   private AtomicInteger purchaseIdCounter = new AtomicInteger(0);


	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}
   
   
// Main operations -------------------------------------------------------

@Override
public List<ItemView> getItems(String productId) throws InvalidItemId_Exception{
	   ProductView productView = null;
	   ItemView itemView;
	   ItemIdView itemIdView;
	   ArrayList<ItemView> _itemViews = new ArrayList<ItemView>();
	   for(Map.Entry<String, SupplierClient> sc: _clients.entrySet()){
		   try{
			   productView = sc.getValue().getProduct(productId);
			   
			   itemView = new ItemView();
			   itemIdView = new ItemIdView();
			   
			   itemIdView.setProductId(productView.getId());
			   itemIdView.setSupplierId(sc.getKey());
			   
			   itemView.setItemId(itemIdView);
			   itemView.setDesc(productView.getDesc());
			   itemView.setPrice(productView.getPrice());
			   
			   _itemViews.add(itemView);
		   } catch(BadProductId_Exception bpe) {throwInvalidItemId("Invalid productId! (Produc Id must be of type string anda can not be null or just empty spaces)!");}
		
		
		   
	   }
	      
	   if(productView == null) {
		   throwInvalidItemId("Can not find product with this productId!");
	   	}
	
	return sortItemViews(_itemViews);
}

@Override
public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
	   List<ItemView> _itemViews = new ArrayList<ItemView>();
	   List<ProductView> _productView = new ArrayList<ProductView>();

	
	for(Map.Entry<String, SupplierClient> sc: _clients.entrySet()){
		try{
				_productView = sc.getValue().searchProducts(descText);
			
					
			   _itemViews = createNewItemView(_productView, _itemViews, sc.getKey());
			
		} catch(BadText_Exception bte) {throwInvalidText("Text can't be empty, null, or just spaces!");}
	}
	
	if(_productView.size()==0)
		throwInvalidText("Product with this Test Dec not found !");
	

	
//	Collections.sort(_itemViews);
	return _itemViews;
}

@Override
public void addToCart(String cartId, ItemIdView itemId, int itemQty)
		throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
	
	if(cartId == null || cartId.length() == 0 ||  "\n".contains(cartId) || "\t".contains(cartId) || "           ".contains(cartId))
		throwInvalidCartId("CartId can not bem null or size 0 or just tabulation charecter or just blank spaces!");
		
		
	CartItemView cartItemView;
	ItemView itemView = null;
	CartView newCv = null;
	CartView cv = null;
	
	
	try	{
		itemView = searchAndCreateItem(itemId, itemQty); // Creates new ItemView from 
	}catch (InvalidQuantity_Exception iqe) {throwInvalidQuantity("Quantity must be a positive value !");}
	catch ( NotEnoughItems_Exception neie) {throwNotEnoughItems("There are not enough items to sell !");}
		
	if(itemView == null)
		throwInvalidItemId("Can not find product with the correspondent itemId! ");
	
	cv = searchCart(cartId);	//Verify if the Cart is already created
	
	if(cv==null){
		newCv = new CartView();
		newCv.setCartId(cartId);
		_carts.put(cartId,newCv);
		cv = newCv;
	}
	
	cartItemView = findProduct(itemId, cv); // verifies if the product is already in the cart
	
	if(cartItemView != null)
		cartItemView.setQuantity(cartItemView.getQuantity() + itemQty);
	
	// If the product does not exist in the cart
	else {
		cartItemView = new CartItemView();	
		cartItemView.setItem(itemView);
		cartItemView.setQuantity(itemQty);
			
		cv.getItems().add(cartItemView);
	
	}		
	
		
			
		}

@Override
public ShoppingResultView buyCart(String cartId, String creditCardNr)
		throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
   
	if(searchCart(cartId) == null)
		throwInvalidCartId("Non existant cart!");
	
	if(isEmpty(cartId) == true)
		throwEmptyCart("The is empty !");
	
	CreditCardClient creditCardClient = null;
	String creditCardUrl = null;
	List<CartItemView> cartItemList = new ArrayList<CartItemView>();
	int quantity = -1;
	int totalPrice = 0;
	int shopId = 0;
	int flagSucess = 0;
	int flagFailure = 0;
	String productId = "";
	String supId = "";
	SupplierClient supClient = null;
	ShoppingResultView shopR = new ShoppingResultView();
	
	try{
	
		creditCardUrl = endpointManager.getUddiNaming().lookup("CreditCard");
	
	} catch(Exception e) {throwInvalidCreditCard("Credit card realeted error! ");}
	
	try {
		creditCardClient = new CreditCardClient(creditCardUrl);
	} catch (CreditCardClientException e) {throwInvalidCreditCard("Error creating credit card!");}
	
	if(!creditCardClient.validateNumber(creditCardNr)){
		throwInvalidCreditCard("Invalid credit card number!");
	}
	
	cartItemList = this._carts.get(cartId).getItems();
	
	for(CartItemView cartItemView: cartItemList){
		quantity = cartItemView.getQuantity();
		productId = cartItemView.getItem().getItemId().getProductId();
		supId = cartItemView.getItem().getItemId().getSupplierId();
		supClient = _clients.get(supId);
				
		try {
			
			if(supClient.buyProduct(productId, quantity)!=null){
				shopR.getPurchasedItems().add(cartItemView);
				totalPrice += cartItemView.getItem().getPrice();
				flagSucess = 1;
			}
			else{
				shopR.getDroppedItems().add(cartItemView);
				flagFailure = 1;
			}
			
		} catch(BadProductId_Exception bpe) {System.out.println("Non existant product !"); }
		catch(BadQuantity_Exception bqe) { System.out.println("Invalid quantity !");}
		catch(InsufficientQuantity_Exception iqe) {System.out.println("There are not enough products!"); }
	}
	shopR.setTotalPrice(totalPrice);
		
	shopR.setId(generatePurchaseId(" "));
		
	if(flagSucess == 1 && flagFailure == 1 )
		shopR.setResult(Result.PARTIAL);
	else if (flagSucess == 0 && flagFailure == 1 )
		shopR.setResult(Result.EMPTY);
	else if (flagSucess == 1 && flagFailure == 0)
		shopR.setResult(Result.COMPLETE);
		
	shopR.setId(generatePurchaseId(""));
	_carts.remove(cartId);
	_purchaseViews.add(0, shopR);
		
	return shopR;
}


// Auxiliary operations --------------------------------------------------	

@Override
public void clear() {
	for(Map.Entry<String, SupplierClient> sc: _clients.entrySet()){
		sc.getValue().clear();
	}
}

public ArrayList<ItemView> sortItemViews(ArrayList<ItemView> _itemViews) {
    Collections.sort(
            _itemViews,
            (itemView1, itemView2) -> itemView1.getPrice()
                    - itemView2.getPrice());
    return _itemViews;
	}

public List<ItemView> createNewItemView(List<ProductView> _productView, List<ItemView> _itemViews, String key){
	   ItemView itemView;
	   ItemIdView itemIdView;
	for(ProductView productView: _productView){
		   itemView = new ItemView();
		   itemIdView = new ItemIdView();
		   
		   itemIdView.setProductId(productView.getId());
		   itemIdView.setSupplierId(key);
		   
		   itemView.setItemId(itemIdView);
		   itemView.setDesc(productView.getDesc());
		   itemView.setPrice(productView.getPrice());
		   
		   _itemViews.add(itemView);
		
	}
	
	
	
	return _itemViews;
}

public boolean isEmpty(String cartId){
	return _carts.get(cartId).getItems().size() == 0; 
}

private String generatePurchaseId(String pid) {
	// relying on AtomicInteger to make sure assigned number is unique
	int purchaseId = purchaseIdCounter.incrementAndGet();
	return Integer.toString(purchaseId);
}

public CartItemView findProduct(ItemIdView itemIdview, CartView cartView){
	List<CartItemView> _list = cartView.getItems();
	ItemView itemView = null;
	ItemIdView newItemIdView = null;
	for(CartItemView cartItemView: _list){
		itemView = cartItemView.getItem();
		newItemIdView = itemView.getItemId();
		if(newItemIdView.getProductId() == itemIdview.getProductId())
			return cartItemView;
			
		
	}
	return null;
}

public ItemView searchAndCreateItem(ItemIdView itemId, int itemQty) throws InvalidQuantity_Exception, NotEnoughItems_Exception {
	ItemView itemView = null;
	ItemIdView itemIdView;
	SupplierClient supplier = _clients.get(itemId.getSupplierId());
	ProductView productView = null;
	
	try {
		productView = supplier.getProduct(itemId.getProductId());
	} catch(Exception e) {}
	
	if(itemQty <=0)
		throwInvalidQuantity("testString");
	
	if(itemQty > productView.getQuantity())
		throwNotEnoughItems("testString2");
	
	itemView = new ItemView();
	itemIdView = new ItemIdView();
	   
	itemIdView.setProductId(productView.getId());
	itemIdView.setSupplierId(itemId.getSupplierId());
	   
	itemView.setItemId(itemIdView);
	itemView.setDesc(productView.getDesc());
	itemView.setPrice(productView.getPrice());
	
	
	
	return itemView;
	
	
}
	
public CartView searchCart(String cartId){
	
	return _carts.get(cartId);
}

@Override
public String ping(String arg0) {
	   String result = "";
	   String defaultName = "A31_Supplier";
	   String supplierUrl = "";
	   
	   
	   SupplierClient client;
	   int i = 0;
	   for(; i<10; i++){
		   try {
		      supplierUrl = endpointManager.getUddiNaming().lookup(defaultName + String.valueOf(i));
		      if(supplierUrl!=null){
		          client = new SupplierClient(supplierUrl);
		          result += client.ping("") + '\n';
		         _clients.put(defaultName + String.valueOf(i),client);
		         
		      }
		   
		   
		   } catch(Exception e) {}
		   
	   }
      if(result.length() != 0)
		   return result.substring(0,result.length()-1); //remove ultimo \n
		else
		   return null;
	}

@Override
public void imAlive() {}


@Override
public List<ShoppingResultView> shopHistory() {
		return _purchaseViews;
	}

@Override
public List<CartView> listCarts() {
	List<CartView> cartList = new ArrayList<CartView>();
	for(Map.Entry<String, CartView> cv: _carts.entrySet()){
		cartList.add(cv.getValue());
	}
	
	return cartList;
}

// View helpers ---------------------------------------------------------


// Exception helpers -----------------------------------------------------

private void throwInsufficientQuantity(final String message) throws InsufficientQuantity_Exception {
	InsufficientQuantity faultInfo = new InsufficientQuantity();
	faultInfo.setMessage(message);
	throw new InsufficientQuantity_Exception(message, faultInfo);
}


private void throwInvalidText(final String message) throws InvalidText_Exception {
	InvalidText faultInfo = new InvalidText();
	faultInfo.message = message;
	throw new InvalidText_Exception(message, faultInfo);
}


private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
	InvalidItemId faultInfo = new InvalidItemId();
	faultInfo.message = message;
	throw new  InvalidItemId_Exception(message, faultInfo);
}

private void throwInvalidCartId(final String message) throws InvalidCartId_Exception{
	InvalidCartId faultInfo = new InvalidCartId();
	faultInfo.message = message;
	throw new InvalidCartId_Exception(message, faultInfo);
}

private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
	InvalidQuantity faultInfo = new InvalidQuantity();
	faultInfo.message = message;
	throw new InvalidQuantity_Exception(message, faultInfo);
}

private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
	NotEnoughItems faultInfo = new NotEnoughItems();
	faultInfo.message = message;
	throw new NotEnoughItems_Exception(message, faultInfo);
}


private void throwEmptyCart(final String message) throws EmptyCart_Exception {
	EmptyCart faultInfo = new EmptyCart();
	faultInfo.message = message;
	throw new EmptyCart_Exception(message, faultInfo);
}

private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
	InvalidCreditCard faultInfo = new InvalidCreditCard();
	faultInfo.message = message;
	throw new InvalidCreditCard_Exception(message, faultInfo);
}

}
