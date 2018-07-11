package com.exploration.cqrs.ecommerce.command;

import java.io.Serializable;

import com.exploration.cqrs.ecommerce.handler.CommandHandler;

public class MarkAsReserved implements Command, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1450042113891118848L;

	private Long commandId;
	private Long inventoryId;
	
	@Override
	public Long getCommandId() {
		return this.commandId;
	}

	public void setCommandId(Long commandId) {
		this.commandId = commandId;
	}

	public Long getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(Long inventoryId) {
		this.inventoryId = inventoryId;
	}

	@Override
	public void acceptHandler(CommandHandler handler) {
		handler.handle(this);
	}

}
