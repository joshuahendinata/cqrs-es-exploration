package com.exploration.cqrs.ecommerce.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.handler.EventHandler;

import io.vertx.reactivex.core.Vertx;

public class EventDispatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcher.class);
	private List<EventHandler> handlersList = new ArrayList<EventHandler>();
	private Vertx context;

	public EventDispatcher(Vertx vertx) {
		this.context = vertx;
	}

	public void register(EventHandler handler) {
		this.handlersList.add(handler);
	}

	public void handleMessage(Event event) {
		handlersList.forEach(eachHandler -> {
			this.context.runOnContext( res -> {
				event.acceptHandler(eachHandler);
			});
		});
	}
}
