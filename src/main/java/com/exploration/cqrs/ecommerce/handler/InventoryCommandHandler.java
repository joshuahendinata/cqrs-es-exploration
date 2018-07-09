package com.exploration.cqrs.ecommerce.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.command.RegisterNewInventory;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

public class InventoryCommandHandler implements CommandHandler<RegisterNewInventory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryCommandHandler.class);
	private JDBCClient hsqlDbClient;
	
	private static String CREATE_INVENTORYS_TABLE = "create table if not exists Inventory "
			+ "(Id BIGINT primary key, "
			+ "Category varchar(255), "
			+ "Description varchar(255), "
			+ "Quantity DECIMAL(10,2),"
			+ "Status varchar(32) )";
	private static String UPDATE_INVENTORY_DETAILS = "update Inventory set Category = ?, Description=?, Quantity = ? where Id = ?";
	private static String UPDATE_INVENTORY_STATUS = "update Inventory set Status = ? where Id = ?";	
	private static String SAVE_INVENTORY = "insert into Inventory (Id, Category, Description, Quantity, Status)"
			+ " values (?, ?, ?, ?, ?)";
	private static String DELETE_INVENTORY = "delete from Inventory where Id = ?";
	
	public InventoryCommandHandler(JDBCClient client) {
		this.hsqlDbClient = client;
		getConnection()
		.flatMapCompletable(conn -> {
			return conn.rxExecute(CREATE_INVENTORYS_TABLE);
		})
		.subscribe();
	}

	@Override
	public void Handle(RegisterNewInventory command) {
		LOGGER.info("inside Handle(RegisterNewInventory)");
		hsqlDbClient.updateWithParams(SAVE_INVENTORY, new JsonArray()
				.add(command.getId())
				.add(command.getCategory())
				.add(command.getDesc())
				.add(command.getQty().doubleValue())
				.add("FreshInventory"), res -> {
					if (res.succeeded()) {
						LOGGER.info("successfully insert into inventory DB");
					} else {
						LOGGER.error("fail insert into inventory DB");
						LOGGER.error(res.cause().getMessage(), res.cause());
					}
				});
	}
	
	private Single<SQLConnection> getConnection() {
		return hsqlDbClient.rxGetConnection().flatMap(conn -> {
			Single<SQLConnection> connectionSingle = Single.just(conn);
			return connectionSingle.doFinally(conn::close); // so after finally, it will be closed
		});
	}

}
