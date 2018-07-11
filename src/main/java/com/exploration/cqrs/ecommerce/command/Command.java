package com.exploration.cqrs.ecommerce.command;

import com.exploration.cqrs.ecommerce.handler.CommandHandler;

public interface Command {
	
	public abstract Long getCommandId();
	
	public abstract void acceptHandler(CommandHandler handler);
}
