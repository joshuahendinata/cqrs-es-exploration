package com.exploration.cqrs.ecommerce;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.handler.EventHandler;
import com.exploration.cqrs.ecommerce.handler.InventoryEventHandler;
import com.exploration.cqrs.ecommerce.infrastructure.EventDispatcher;
import com.exploration.cqrs.ecommerce.readmodel.InventoryReadModelDao;
import com.exploration.cqrs.ecommerce.util.SerDesUtil;

import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

public class ReadVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadVerticle.class);
	private EventHandler InventoryEventHandler;
	private EventDispatcher eventDispatcher;
	private InventoryReadModelDao inventoryDao;
	
	@Override
	public void init(Vertx vertx, Context context) {
	    super.init(vertx, context);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		
		this.inventoryDao = new InventoryReadModelDao(
		    		MongoClient.createShared(vertx, new JsonObject()
		    				.put("db_name", "cqrs-exploration")
		    				.put("host", "ds125851.mlab.com")
		    				.put("port", 25851)
		    				.put("username", "admin")
		    				.put("password", "admin123")
		    				,"readModelPool"));
		
		// instead of communicating via HTTP, use event bus (which is essentially a message queue)
		// publish a "Record" containing the name of the bus that this verticle is consuming
		
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		
		// Check if bus is already deployed. If yes, use the bus. If not, publish the bus
		
		
		/* Method 1 based on merging stream. 
		 * (-) multiple call to rxGetRecord
		 * 
		Single<Record> recordSource = discovery.rxGetRecord(rec -> {
			LOGGER.info("inside rxGetRecord filter");
			return "event-bus-name".equals(rec.getType()) && "read".equals(rec.getName());
		});
		
		Maybe<Record> publishNewRecord = recordSource
				.filter(rec -> {
					return rec == null;
				})
				.flatMapSingleElement(rec -> {
					return discovery.rxPublish(new Record()
				    .setType("event-bus-name")
				    .setName("read")
				    .setLocation(new JsonObject().put("qName", "read-event-bus")));
				});
		
		// Need to put publishNewRecord first because it is the only way "Maybe" can be merged with other stream.
		// The way merge works is by calling each record through the stream before going to the next record
		publishNewRecord
		.mergeWith(recordSource.toMaybe())
		.doOnError(err -> {
    		LOGGER.error(err.getMessage(), err);
    		startFuture.failed();
		})
		.doFinally(() -> {
			discovery.close();
			startFuture.complete();
		})
		.subscribe(rec -> {
			if (rec == null) {
				return;
			}
    		LOGGER.info("successfully get read record. Reg ID: " + rec.getRegistration());
			String eventBusName = rec.getLocation().getString("qName");
			vertx.eventBus().<JsonObject>consumer(eventBusName, this::onMessage);
			
		});	*/
		
		
		// Method 2. Use flatMap to combine rxPublish into the stream
		discovery.rxGetRecord(rec -> {
			LOGGER.info("inside rxGetRecord filter");
			return "event-bus-name".equals(rec.getType()) && "read".equals(rec.getName());
		}).flatMap(res -> {
			
			// If failed or event bus not found, publish new one
			if (res == null) {
				return discovery.rxPublish(new Record()
				    .setType("event-bus-name")
				    .setName("read")
				    .setLocation(new JsonObject().put("qName", "read-event-bus")));
			} 
			
			return Single.just(res);
		})
		.doOnError(err -> {
    		LOGGER.error(err.getMessage(), err);
    		startFuture.failed();
		})
		.doFinally(() -> {
			discovery.close();
			startFuture.complete();
		})
		.subscribe(res -> {
    		LOGGER.info("successfully get read record. Reg ID: " + res.getRegistration());
			String eventBusName = res.getLocation().getString("qName");
			vertx.eventBus().<JsonObject>consumer(eventBusName, this::onMessage);
			
			// Set up Kafka bus for interacting with event handler
			Properties conConfig = new Properties();
			conConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			conConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
			conConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SerDesUtil.class);
			conConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "event_consumer_group");
			conConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
			conConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
			
			KafkaConsumer<Long, Event> consumer = KafkaConsumer.create(vertx.getDelegate(), conConfig);
			consumer.subscribe("EVT_TOPIC", ar -> {
				if (ar.succeeded()) {
					LOGGER.debug("subscribed to EVT_TOPIC");
				} else {
					LOGGER.error("Could not subscribe " + ar.cause().getMessage());
				}
			});

			this.eventDispatcher = new EventDispatcher(this.vertx);
			this.InventoryEventHandler = new InventoryEventHandler(this.vertx);
			
			// set commandHandler to handle anything through command bus
			this.eventDispatcher.register(this.InventoryEventHandler);
			consumer.handler(record -> {
				eventDispatcher.handleMessage(record.value());
			});
			
		});
		
		
		// This is for HTTP request microservice
		/*
		
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		
		// Router for handling service request
		router.post("/getAllInventory").handler(this::getAllInventory);
		
		int port = config().getInteger(ProjectConstants.CONFIG_HTTP_SERVER_PORT, 8081);
		
		server.requestHandler(router::accept)
		.rxListen(port)
		.flatMap(ar -> {
				LOGGER.info("ReadVerticle server running on port " + port);
				//startFuture.complete();
				
				Record record = HttpEndpoint.createRecord("read", "localhost", // can be retrieved from API 
						port, "/");
				
				return discovery.rxPublish(record);
		})
		.doOnError(err -> {
			LOGGER.error(err.getMessage(), err);
			startFuture.fail(err);
		})
		.doFinally(() -> discovery.close())
		.subscribe(ar -> {
			// publication succeeded
			this.publishedRecord = ar;
			this.registrationId = ar.getRegistration();
			LOGGER.info("ReadVerticle registered to discovery. ID:" + this.registrationId);
			startFuture.complete();
		});*/
		
		
		
	}
	
	public void onMessage(Message<JsonObject> message) {
		if (!message.headers().contains("action")) {
			LOGGER.error("No action header specified for message with headers {} and body {}", message.headers(),
					message.body().encodePrettily());
			message.fail(404, "invalid command");
			return;
		}
		
		String action = message.headers().get("action");
		switch (action) {
		case "getFreshInventory":
			getFreshInventory(message);
			break;
		case "getReservedInventory":
			getReservedInventory(message);
			break;
		default: 
			message.fail(404, "invalid command:" + action);
		}
	}


	private void getReservedInventory(Message<JsonObject> message) {
		LOGGER.info("inside getReservedInventory");
		this.inventoryDao.findReservedInventory()
		.doOnError(err->{
			message.fail(1, err.getMessage());
		}).subscribe(res ->{
			message.reply(new JsonObject().put("result", new JsonArray(res)));
		});
	}

	private void getFreshInventory(Message<JsonObject> message) {
		LOGGER.info("inside all inventory");
		this.inventoryDao.findFreshInventory()
		.doOnError(err->{
			message.fail(1, err.getMessage());
		}).subscribe(res ->{
			message.reply(new JsonObject().put("result", new JsonArray(res)));
		});
	}
}
