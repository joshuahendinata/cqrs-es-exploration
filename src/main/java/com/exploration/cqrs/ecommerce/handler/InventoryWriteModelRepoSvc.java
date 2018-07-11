package com.exploration.cqrs.ecommerce.handler;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.boundedcontext.InventoryContext;
import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.util.SerDesUtil;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

public class InventoryWriteModelRepoSvc implements WriteModelRepoSvc<InventoryContext> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryWriteModelRepoSvc.class);
	private JDBCClient hsqlDbClient;
	private KafkaProducer<Long, Event> eventBus;
	
	private static String CREATE_INVENTORYS_TABLE = "create table if not exists Inventory "
			+ "(Id BIGINT primary key, "
			+ "Category varchar(255), "
			+ "Description varchar(255), "
			+ "Quantity DECIMAL(10,2),"
			+ "Status varchar(32),"
			+ "SoldQuantity Decimal(10,2) )";
	private static String UPDATE_INVENTORY_DETAILS = "update Inventory set Category = ?, Description=?, Quantity = ? where Id = ?";
	private static String UPDATE_INVENTORY_STATUS = "update Inventory set Status = ? where Id = ?";	
	private static String SAVE_INVENTORY = "insert into Inventory (Id, Category, Description, Quantity, Status, SoldQuantity)"
			+ " values (?, ?, ?, ?, ?, ?)";
	private static String DELETE_INVENTORY = "delete from Inventory where Id = ?";
	
	public InventoryWriteModelRepoSvc(JDBCClient client, KafkaProducer<Long, Event> eventBus) {
		this.hsqlDbClient = client;
		this.eventBus = eventBus;
		getConnection()
		.flatMapCompletable(conn -> {
			return conn.rxExecute(CREATE_INVENTORYS_TABLE);
		})
		.subscribe();
	}
	
	public InventoryWriteModelRepoSvc(Vertx vertx) {

		this.hsqlDbClient = JDBCClient.createShared(vertx, new JsonObject()
	    	    .put("url", "jdbc:hsqldb:file:db/cqrs-exploration")   
	    	    .put("driver_class", "org.hsqldb.jdbcDriver")   
	    	    .put("max_pool_size", 30));

		Properties prodConfig = new Properties();
		prodConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		prodConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
		prodConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SerDesUtil.class);
		prodConfig.put(ProducerConfig.ACKS_CONFIG, "1");

		// use producer for interacting with Apache Kafka
		this.eventBus = KafkaProducer.create(vertx.getDelegate(), prodConfig);
	}

	private Single<SQLConnection> getConnection() {
		return hsqlDbClient.rxGetConnection().flatMap(conn -> {
			Single<SQLConnection> connectionSingle = Single.just(conn);
			return connectionSingle.doFinally(conn::close); // so after finally, it will be closed
		});
	}

	@Override
	public InventoryContext findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(InventoryContext objectToBeSaved) {
		LOGGER.info("inside InventoryRepositoryService.save()");
		hsqlDbClient.updateWithParams(SAVE_INVENTORY, new JsonArray()
				.add(objectToBeSaved.getId())
				.add(objectToBeSaved.getCategory())
				.add(objectToBeSaved.getDescription())
				.add(objectToBeSaved.getQuantity())
				.add("FreshInventory")
				.add(objectToBeSaved.getSoldQuantity()), res -> {
					if (res.succeeded()) {
						InventoryRegistered event = new InventoryRegistered();
						event.setSourceId(objectToBeSaved.getId());
						event.setNewInventory(objectToBeSaved);
						
						LOGGER.info("successfully insert into inventory DB");
						this.eventBus.write(KafkaProducerRecord.create("EVT_TOPIC", 
								event.getSourceId(), event));
					} else {
						LOGGER.error("fail insert into inventory DB");
						LOGGER.error(res.cause().getMessage(), res.cause());
					}
				});
	}
}
