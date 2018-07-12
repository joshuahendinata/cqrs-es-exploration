package com.exploration.cqrs.ecommerce.boundedcontext;

import java.util.ArrayList;
import java.util.List;

import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.event.InventoryReserved;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class EventSourcedBoundedContext implements BoundedContext{

	@JsonIgnore
	private List<Event> pendingEvents = new ArrayList<Event>();
	
	@JsonIgnore
	private int version = -1;
	
	public void loadFrom(List<Event> pastEvents) {
		pastEvents.forEach(eachEvent -> {
			eachEvent.acceptBoundedContext(this);
			this.version = eachEvent.getVersion();
		});
	}
	
	public void update(Event e) {
		this.pendingEvents.add(e);
	}

	public List<Event> getPendingEvents() {
		return pendingEvents;
	}

	public void setPendingEvents(List<Event> pendingEvents) {
		this.pendingEvents = pendingEvents;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/*
	 * To be overridden if necessary
	 */
	public void onEvent(InventoryRegistered inventoryRegistered) {}
	public void onEvent(InventoryReserved inventoryReserved) {}
}
