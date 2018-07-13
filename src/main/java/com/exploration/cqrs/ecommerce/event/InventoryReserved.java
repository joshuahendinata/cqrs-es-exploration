package com.exploration.cqrs.ecommerce.event;

import java.util.Date;

import com.exploration.cqrs.ecommerce.boundedcontext.EventSourcedBoundedContext;
import com.exploration.cqrs.ecommerce.handler.EventHandler;

public class InventoryReserved extends Event {

	private Long sourceId;
	private String reservedBy;
	private Date reservedDate;
	
	@Override
	public Long getSourceId() {
		return this.sourceId;
	}
	
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public String getReservedBy() {
		return reservedBy;
	}

	public void setReservedBy(String reservedBy) {
		this.reservedBy = reservedBy;
	}

	public Date getReservedDate() {
		return reservedDate;
	}

	public void setReservedDate(Date reservedDate) {
		this.reservedDate = reservedDate;
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
