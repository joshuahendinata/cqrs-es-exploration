package com.exploration.cqrs.ecommerce.event;

import java.io.Serializable;

import com.exploration.cqrs.ecommerce.boundedcontext.InventoryContext;
import com.exploration.cqrs.ecommerce.handler.EventHandler;

public class InventoryRegistered implements Event, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 69351533225504747L;
	private Long sourceId;
	private InventoryContext newInventory; 

	@Override
	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long eventId) {
		this.sourceId = eventId;
	}

	public InventoryContext getNewInventory() {
		return newInventory;
	}

	public void setNewInventory(InventoryContext newInventory) {
		this.newInventory = newInventory;
	}

	@Override
	public void acceptHandler(EventHandler handler) {
		handler.handle(this);
	}

}
