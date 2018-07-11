package com.exploration.cqrs.ecommerce.event;

import com.exploration.cqrs.ecommerce.handler.EventHandler;

public interface Event {
	
	public Long getSourceId();
	
	public void acceptHandler(EventHandler handler);
}
