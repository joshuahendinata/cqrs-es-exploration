package com.exploration.cqrs.ecommerce.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.CommandVerticle;
import com.exploration.cqrs.ecommerce.command.Command;
import com.exploration.cqrs.ecommerce.handler.CommandHandler;

import io.vertx.reactivex.core.Vertx;

public class CommandDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandVerticle.class);
	
	private Map<String, List<CommandHandler>> handlers = new HashMap<String, List<CommandHandler>>();
	private Vertx context;
	private List<CommandHandler> handlersList = new ArrayList<CommandHandler>();
	
	public CommandDispatcher(Vertx vertx) {
		this.context = vertx;
	}

	public void register(CommandHandler handler) {
		handlersList.add(handler);
	}
	
	public void handleMessage(Command command) {		
		handlersList.forEach(eachHandler -> {
			this.context.runOnContext( res -> {
				command.acceptHandler(eachHandler);
			});
		});
	}
}
