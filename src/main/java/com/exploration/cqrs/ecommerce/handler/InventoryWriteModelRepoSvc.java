package com.exploration.cqrs.ecommerce.handler;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exploration.cqrs.ecommerce.boundedcontext.InventoryContext;
import com.exploration.cqrs.ecommerce.event.Event;
import com.exploration.cqrs.ecommerce.util.SerDesUtil;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.pattern.PatternsCS;
import eventstore.EsException;
import eventstore.EventData;
import eventstore.ReadStreamEvents;
import eventstore.ReadStreamEventsCompleted;
import eventstore.Settings;
import eventstore.WriteEventsCompleted;
import eventstore.j.EventDataBuilder;
import eventstore.j.ReadStreamEventsBuilder;
import eventstore.j.SettingsBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;
import io.reactivex.Observable;
import io.reactivex.Single;
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
	private Vertx context;
	
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
	
	/*public InventoryWriteModelRepoSvc(JDBCClient client, KafkaProducer<Long, Event> eventBus) {
		this.hsqlDbClient = client;
		this.eventBus = eventBus;
		getConnection()
		.flatMapCompletable(conn -> {
			return conn.rxExecute(CREATE_INVENTORYS_TABLE);
		})
		.subscribe();
	}*/
	
	public InventoryWriteModelRepoSvc(Vertx vertx) {
		this.context = vertx;
		
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
	public Single<InventoryContext> findById(Long id) {
		final ActorSystem system = ActorSystem.create();
		final Settings settings = new SettingsBuilder().address(new InetSocketAddress("127.0.0.1", 1113))
				.defaultCredentials("admin", "changeit").build();

		final ActorRef connection = system.actorOf(ConnectionActor.getProps(settings));
		// final ActorRef readResult = system.actorOf(Props.create(ReadResult.class));

		final ReadStreamEvents readEvent = new ReadStreamEventsBuilder(InventoryContext.class.getSimpleName() + "-" + id)
				.forward()
				.fromFirst()
				.resolveLinkTos(false)
				.requireMaster(true)
				.build();

		// connection.tell(readEvent, readResult);

		return Observable.fromFuture(PatternsCS.ask(connection, readEvent, 60000).toCompletableFuture())
			.flatMap(readEventResult -> {
				if (readEventResult instanceof ReadStreamEventsCompleted) {
					ReadStreamEventsCompleted completed = (ReadStreamEventsCompleted) readEventResult;
					return Observable.fromIterable(completed.eventsJava());
				} else {
					return null;
				}
			}).map(eachEvent -> {
				JsonObject eventJson = new JsonObject(eachEvent.data().data().value().decodeString(Charset.defaultCharset()));
				Event cqrsEvent = (Event) eventJson.mapTo(Class.forName(eventJson.getString("className")));
				return cqrsEvent;
			}).reduce(new InventoryContext(), (invContext, reducer) -> {
				reducer.acceptBoundedContext(invContext);
				return invContext;
			});

	}

	@Override
	public void save(InventoryContext objectToBeSaved) {
		LOGGER.info("inside InventoryRepositoryService.save()");
		
		/*
		 * This is for HSQLDB before we tried event sourcing 
		 */
		/*hsqlDbClient.updateWithParams(SAVE_INVENTORY, new JsonArray()
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
						this.eventBus.write(KafkaProducerRecord.create("EVT_TOPIC2", 
								event.getSourceId(), event), done -> {
									if (done.succeeded()) {
										RecordMetadata recordMetadata = done.result();
										LOGGER.info("Message written on topic=" + recordMetadata.getTopic() + ", partition="
												+ recordMetadata.getPartition() + ", offset=" + recordMetadata.getOffset());

										
									} else {
										LOGGER.error(done.cause().getMessage(), done.cause());
										
									}
								});
					} else {
						LOGGER.error("fail insert into inventory DB");
						LOGGER.error(res.cause().getMessage(), res.cause());
					}
				});*/
		
		final ActorSystem system = ActorSystem.create();
        final ActorRef connection = system.actorOf(ConnectionActor.getProps());
        final ActorRef writeResult = system.actorOf(Props.create(WriteResult.class));

        final WriteEventsBuilder writeEventsBuilder = 
        		new WriteEventsBuilder(InventoryContext.class.getSimpleName() + "-" + objectToBeSaved.getId())
                .expectAnyVersion(); // TODO change this for version-ing guard
        
        Observable.fromIterable(objectToBeSaved.getPendingEvents())
        .<EventData>map(pendingEvt -> {
        	return new EventDataBuilder("cqrs-event")
                    .eventId(UUID.randomUUID())
                    .jsonData(JsonObject.mapFrom(pendingEvt).encode())
                    //.metadata("my first event")
                    .build();
        })
        .doFinally(() -> {
            connection.tell(writeEventsBuilder.build(), writeResult);

    		objectToBeSaved.getPendingEvents().forEach(eachEvent -> {
        		this.eventBus.write(KafkaProducerRecord.create("EVT_TOPIC", 
        				eachEvent.getSourceId(), eachEvent));
            });
        })
        .subscribe(ed -> {
        	writeEventsBuilder.addEvent(ed);
        });
	}
	
	public static class WriteResult extends AbstractActor  {
				
		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(WriteEventsCompleted.class, message -> {
	                final WriteEventsCompleted completed = (WriteEventsCompleted) message;
	                LOGGER.info("range: {}, position: {}", completed.numbersRange(), completed.position());
	                context().system().terminate();
				})
				.match(Status.Failure.class, message -> {
					final Status.Failure failure = ((Status.Failure) message);
	                final EsException exception = (EsException) failure.cause();
	                LOGGER.error(exception.toString(), exception);
	                context().system().terminate();
				})
				.build();
		}
    }
	
	/*public class ReadResult extends AbstractActor  {
		
		public List<eventstore.Event> eventList = new ArrayList<eventstore.Event>();
		
		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(ReadStreamEventsCompleted.class, message -> {
	                final ReadStreamEventsCompleted completed = (ReadStreamEventsCompleted) message;
	                eventList.addAll(completed.eventsJava());
	                LOGGER.info("event count: {}", eventList.size());
	                context().system().terminate();
				})
				.match(Status.Failure.class, message -> {
					final Status.Failure failure = ((Status.Failure) message);
	                final EsException exception = (EsException) failure.cause();
	                LOGGER.error(exception.toString(), exception);
	                context().system().terminate();
				})
				.build();
		}

		public List<eventstore.Event> getEventList() {
			return eventList;
		}

		public void setEventList(List<eventstore.Event> eventList) {
			this.eventList = eventList;
		}
		
    }*/
}
