package com.exploration.cqrs.ecommerce.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.event.InventoryReserved;
import com.exploration.cqrs.ecommerce.readmodel.FreshInventory;
import com.exploration.cqrs.ecommerce.readmodel.InventoryReadModelDao;
import com.exploration.cqrs.ecommerce.readmodel.ReservedInventory;

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
		freshInventory.set_id(event.getSourceId());
		freshInventory.setName(event.getInventoryName());
		freshInventory.setDescription(event.getInventoryDescription());
		freshInventory.setQuantity(event.getInventoryQuantity());
		freshInventory.setCategory(event.getInventoryCategory());
		
		this.inventoryDao.save(freshInventory, "freshInventory");
	}
	
	@Override
	public void handle(InventoryReserved event) {
		JsonObject query = new JsonObject().put("_id", event.getSourceId());
		// TODO create 1 more collection for reserved inventory
		this.inventoryDao.removeOneAndReturn(query, "freshInventory")
		.map(json -> {
			ReservedInventory reservedInv = new ReservedInventory();
			
			reservedInv.set_id(json.getLong("_id"));
			reservedInv.setName(json.getString("name"));
			reservedInv.setDescription(json.getString("description"));
			reservedInv.setQuantity(json.getDouble("quantity"));
			reservedInv.setCategory(json.getString("category"));
			reservedInv.setReservedBy(event.getReservedBy());
			reservedInv.setReservedDate(event.getReservedDate());
			
			return reservedInv;
		})
		.subscribe(res -> {
			this.inventoryDao.save(res, "reservedInventory");
		});
	}

}
