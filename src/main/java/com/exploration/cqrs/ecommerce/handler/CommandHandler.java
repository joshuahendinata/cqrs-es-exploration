package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.command.MarkAsReserved;
import com.exploration.cqrs.ecommerce.command.RegisterNewInventory;

/**
 * Abstract class for command handler. 
 * The extending class should only override the necessary method. 
 * This way, if a command is added, you only need to add one method here
 * @author Joshua
 *
 */
public abstract class CommandHandler{
	
	public void handle(MarkAsReserved command) {}

	public void handle(RegisterNewInventory command) {}
}
