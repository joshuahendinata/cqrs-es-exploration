package com.exploration.cqrs.ecommerce.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.boundedcontext.InventoryContext;
import com.exploration.cqrs.ecommerce.command.MarkAsReserved;
import com.exploration.cqrs.ecommerce.command.RegisterNewInventory;

import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

public class InventoryCommandHandler extends CommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryCommandHandler.class);
	private WriteModelRepoSvc<InventoryContext> repository;
	
	public InventoryCommandHandler(Vertx vertx) {
		this.repository = new InventoryWriteModelRepoSvc(vertx);
	}

	@Override
	public void handle(RegisterNewInventory command) {
		LOGGER.info("inside Handle(RegisterNewInventory)");
		InventoryContext inv = new InventoryContext(
				System.currentTimeMillis(), 
				command.getName(), 
				command.getDesc(),
				command.getQty(),
				command.getCategory());
		
		repository.save(inv);
	}

	@Override
	public void handle(MarkAsReserved command) {
		LOGGER.info("inside Handle(MarkAsReserved)");
		Single<InventoryContext> inv = repository.findById(command.getInventoryId());
		inv.subscribe(res -> {
			if ("Reserved".equals(res.getStatus())) {
				return;
			}
			res.markAsReserved(command);
			repository.save(res);
		});
	}
}
