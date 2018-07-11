package com.exploration.cqrs.ecommerce.infrastructure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.handler.EventHandler;

import io.vertx.reactivex.core.Vertx;

public class EventDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcher.class);

	private Map<String, List<EventHandler>> handlers = new HashMap<String, List<EventHandler>>();
	private Vertx context;

	public EventDispatcher(Vertx vertx) {
		this.context = vertx;
	}

	public void register(EventHandler handler) {
		for (Type iface : handler.getClass().getGenericInterfaces()) {
			if (iface instanceof ParameterizedType) {
				for (Type paramType : ((ParameterizedType) iface).getActualTypeArguments()) {
					handlers.putIfAbsent(paramType.getTypeName(), new ArrayList<EventHandler>());
					handlers.get(paramType.getTypeName()).add(handler);
				}
			}
		}

	}

	public void handleMessage(Event event) {
		List<EventHandler> handlersList = handlers.get(event.getClass().getName());
		if (handlersList == null || handlersList.isEmpty()) {
			return; // TODO might need to throw exception
		}

		for (EventHandler eachHandler : handlersList) {
			LOGGER.info("Event: " + event.getClass().getSimpleName() + " is being handled by:"
					+ eachHandler.getClass().getSimpleName());
			this.context.runOnContext(res -> {
				eachHandler.handle(event);
			});
		}
	}
}
