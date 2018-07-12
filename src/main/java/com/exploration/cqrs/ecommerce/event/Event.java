package com.exploration.cqrs.ecommerce.event;

import com.exploration.cqrs.ecommerce.boundedcontext.EventSourcedBoundedContext;
import com.exploration.cqrs.ecommerce.handler.EventHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vertx.core.json.JsonObject;

public abstract class Event {
	
	@JsonIgnore
	public Integer version = -1;
	
	public String className = this.getClass().getName();
	
	public abstract Long getSourceId();
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 *  Visitor pattern for eventHandler. 
	 *  Implement handler.handle(this);
	 */
	public abstract void acceptHandler(EventHandler handler);
	
	/** 
	 * Visitor pattern for boundedContext. 
	 * implement bc.onEvent(this);
	 */
	public abstract void acceptBoundedContext(EventSourcedBoundedContext bc);
}
