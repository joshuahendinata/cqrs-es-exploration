package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.command.Command;

public interface CommandHandler<T extends Command>{

	public void Handle(T command);
}
