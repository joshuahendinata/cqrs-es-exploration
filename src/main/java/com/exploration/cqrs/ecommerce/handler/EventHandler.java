package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.event.InventoryRegistered;

public abstract class EventHandler {
	
	public void handle(InventoryRegistered event) {}
}
