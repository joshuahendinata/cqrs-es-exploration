package com.exploration.cqrs.ecommerce.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.exploration.cqrs.ecommerce.command.Command;
import com.exploration.cqrs.ecommerce.command.CommandVerticle;

import io.vertx.reactivex.core.Vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandVerticle.class);
	
	private Map<String, List<CommandHandler>> handlers = new HashMap<String, List<CommandHandler>>();
	private Vertx context;
	
	public CommandDispatcher(Vertx vertx) {
		this.context = vertx;
	}

	public void register(CommandHandler handler) {
		for (Type iface : handler.getClass().getGenericInterfaces()) {
			if (iface instanceof ParameterizedType) {
				for (Type paramType : ((ParameterizedType) iface).getActualTypeArguments()) {
					handlers.putIfAbsent(paramType.getTypeName(), new ArrayList<CommandHandler>());
					handlers.get(paramType.getTypeName()).add(handler);					
				}
			}
		}
		
	}
	
	public void handleMessage(Command command) {
		List<CommandHandler> handlersList = handlers.get(command.getClass().getName());
		if (handlersList == null || handlersList.isEmpty()) {
			return; //TODO might need to throw exception
		}
		
		for (CommandHandler eachHandler : handlersList) {
			LOGGER.info("Command: " + command.getClass().getSimpleName() 
					+ " is being handled by:" + eachHandler.getClass().getSimpleName());
			this.context.runOnContext( res -> {
				eachHandler.Handle(command);
			});
		}
	}
}
