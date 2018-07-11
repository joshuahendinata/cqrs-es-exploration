package com.exploration.cqrs.ecommerce.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.readmodel.FreshInventory;
import com.exploration.cqrs.ecommerce.readmodel.InventoryReadModelDao;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;

/**
 * In CQRS Journey, they separate the event handler to viewModel generator and bounded context handler
 * In this scenario, this handler will cater to both.
 *  
 * @author Joshua
 *
 */
public class InventoryEventHandler extends EventHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventHandler.class);
	
	// For now we skip dependency injection first
	private	InventoryReadModelDao inventoryDao;
	
	public InventoryEventHandler(Vertx vertx) {
		this.inventoryDao = new InventoryReadModelDao(
	    		MongoClient.createShared(vertx, new JsonObject()
	    				.put("db_name", "cqrs-exploration")
	    				.put("host", "ds125851.mlab.com")
	    				.put("port", 25851)
	    				.put("username", "admin")
	    				.put("password", "admin123")
	    				,"readModelPool"));
	}

	@Override
	public void handle(InventoryRegistered event) {
		FreshInventory freshInventory = new FreshInventory();
		freshInventory.setId(event.getSourceId());
		freshInventory.setName(event.getNewInventory().getName());
		freshInventory.setDescription(event.getNewInventory().getDescription());
		freshInventory.setQuantity(event.getNewInventory().getQuantity());
		freshInventory.setCategory(event.getNewInventory().getCategory());
		
		this.inventoryDao.save(freshInventory);
	}

}
