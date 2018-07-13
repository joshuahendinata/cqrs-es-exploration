package com.exploration.cqrs.ecommerce.event;

import java.io.Serializable;

import com.exploration.cqrs.ecommerce.boundedcontext.EventSourcedBoundedContext;
import com.exploration.cqrs.ecommerce.handler.EventHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class InventoryRegistered extends Event{

	private Long sourceId;
	private String inventoryName;
	private String inventoryDescription;
	private Double inventoryQuantity;
	private String inventoryCategory;
	
	@Override
	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long eventId) {
		this.sourceId = eventId;
	}

	public String getInventoryName() {
		return inventoryName;
	}

	public void setInventoryName(String inventoryName) {
		this.inventoryName = inventoryName;
	}

	public String getInventoryDescription() {
		return inventoryDescription;
	}

	public void setInventoryDescription(String inventoryDescription) {
		this.inventoryDescription = inventoryDescription;
	}

	public Double getInventoryQuantity() {
		return inventoryQuantity;
	}

	public void setInventoryQuantity(Double inventoryQuantity) {
		this.inventoryQuantity = inventoryQuantity;
	}

	public String getInventoryCategory() {
		return inventoryCategory;
	}

	public void setInventoryCategory(String inventoryCategory) {
		this.inventoryCategory = inventoryCategory;
	}
	
	@Override
	public void acceptHandler(EventHandler handler) {
		handler.handle(this);
	}

	@Override
	public void acceptBoundedContext(EventSourcedBoundedContext bc) {
		bc.onEvent(this);
	}

}
