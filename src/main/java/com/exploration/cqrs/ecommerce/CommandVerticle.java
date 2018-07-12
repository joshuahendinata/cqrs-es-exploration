package com.exploration.cqrs.ecommerce;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.command.Command;
import com.exploration.cqrs.ecommerce.command.MarkAsReserved;
import com.exploration.cqrs.ecommerce.command.RegisterNewInventory;
import com.exploration.cqrs.ecommerce.handler.CommandHandler;
import com.exploration.cqrs.ecommerce.handler.InventoryCommandHandler;
import com.exploration.cqrs.ecommerce.infrastructure.CommandDispatcher;
import com.exploration.cqrs.ecommerce.util.SerDesUtil;

import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

public class CommandVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandVerticle.class);
	private CommandHandler inventoryCommandHandler; 
	private KafkaProducer<Long, Command> commandBus;
	private CommandDispatcher commandDispatcher;
	
	@Override
	public void init(Vertx vertx, Context context) {
	    super.init(vertx, context);
	}
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
		
		// Check if bus is already deployed. If yes, use the bus. If not, publish the bus
		discovery.rxGetRecord(rec -> {
			LOGGER.info("inside rxGetRecord filter");
			return "event-bus-name".equals(rec.getType()) && "write".equals(rec.getName());
		}).flatMap(res -> {
			
			// If failed or event bus not found, publish new one
			if (res == null) {
				return discovery.rxPublish(new Record()
				    .setType("event-bus-name")
				    .setName("write")
				    .setLocation(new JsonObject().put("qName", "write-event-bus")));
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
    		LOGGER.info("successfully get write record. Reg ID: " + res.getRegistration());
			String eventBusName = res.getLocation().getString("qName");
			vertx.eventBus().<JsonObject>consumer(eventBusName, this::onMessage);
			
			// Set up Kafka bus for interacting with command handler
			Properties conConfig = new Properties();
			conConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			conConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
			conConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SerDesUtil.class);
			conConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "command_consumer_group");
			conConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
			conConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
			
			KafkaConsumer<Long, Command> consumer = KafkaConsumer.create(vertx.getDelegate(), conConfig);
			consumer.subscribe("CMD_TOPIC", ar -> {
				if (ar.succeeded()) {
					LOGGER.debug("subscribed to CMD_TOPIC");
				} else {
					LOGGER.error("Could not subscribe " + ar.cause().getMessage());
				}
			});
			
			// TODO Below can be improved by having dependency injection (for further improvement)
			this.commandDispatcher = new CommandDispatcher(this.vertx);
			this.inventoryCommandHandler = new InventoryCommandHandler(this.vertx);

			// set commandHandler to handle anything through command bus
			this.commandDispatcher.register(this.inventoryCommandHandler);
			consumer.handler(record -> {
				commandDispatcher.handleMessage(record.value());
			});
			
			Properties prodConfig = new Properties();
			prodConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
			prodConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
			prodConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SerDesUtil.class);
			prodConfig.put(ProducerConfig.ACKS_CONFIG, "1");

			// use producer for interacting with Apache Kafka
			this.commandBus = KafkaProducer.create(vertx.getDelegate(), prodConfig);
			// TODO implement ProducerInterceptor to catch error 
		});
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
		case "createNewInventory":
			createNewInventory(message);
			break;
		case "markAsReserved":
			markAsReserved(message);
			break;
		default: 
			message.fail(404, "invalid command:" + action);
		}
	}


	private void markAsReserved(Message<JsonObject> message) {
		MarkAsReserved command = new MarkAsReserved();
		command.setCommandId(System.currentTimeMillis());
		command.setInventoryId(Long.valueOf(message.body().getString("inventoryId")));
		
		// Send to command bus
		this.commandBus.write(KafkaProducerRecord.create("CMD_TOPIC", command.getCommandId(), 
				command), done -> {
				if (done.succeeded()) {
					RecordMetadata recordMetadata = done.result();
					LOGGER.info("Message written on topic=" + recordMetadata.getTopic() + ", partition="
							+ recordMetadata.getPartition() + ", offset=" + recordMetadata.getOffset());

					message.reply(new JsonObject().put("response", "success"));
				} else {
					LOGGER.error(done.cause().getMessage(), done.cause());
					message.fail(1, done.cause().getMessage());
				}
			});
	}

	private void createNewInventory(Message<JsonObject> message) {
		// TODO some validation will be here
		JsonObject body = message.body();
		RegisterNewInventory registerNewInventory = new RegisterNewInventory();
		
		//Create ID here because it's a new inventory
		registerNewInventory.setCommandId(System.currentTimeMillis()); 
		registerNewInventory.setName(body.getString("name"));
		registerNewInventory.setDesc(body.getString("desc"));
		registerNewInventory.setCategory(body.getString("category"));
		registerNewInventory.setQty(Double.valueOf(body.getString("qty")));

		// Send to command bus
		this.commandBus.write(KafkaProducerRecord.create("CMD_TOPIC", registerNewInventory.getCommandId(), 
			registerNewInventory), done -> {
				if (done.succeeded()) {
					RecordMetadata recordMetadata = done.result();
					LOGGER.info("Message written on topic=" + recordMetadata.getTopic() + ", partition="
							+ recordMetadata.getPartition() + ", offset=" + recordMetadata.getOffset());

					message.reply(new JsonObject().put("response", "success"));
				} else {
					LOGGER.error(done.cause().getMessage(), done.cause());
					message.fail(1, done.cause().getMessage());
				}
			});
	}
}
