package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.event.InventoryReserved;

public abstract class EventHandler {
	
	public void handle(InventoryRegistered event) {}

	public void handle(InventoryReserved inventoryReserved) {}
}
