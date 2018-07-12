package com.exploration.cqrs.ecommerce.event;

import com.exploration.cqrs.ecommerce.boundedcontext.EventSourcedBoundedContext;
import com.exploration.cqrs.ecommerce.handler.EventHandler;

public class InventoryReserved extends Event {

	private Long sourceId;
	
	@Override
	public Long getSourceId() {
		return this.sourceId;
	}
	
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
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
