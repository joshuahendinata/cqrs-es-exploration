# cqrs-es-exploration
Exploration of CQRS and Event Sourcing (ES) concept in general

This project implements CQRS and ES concept based on [microsoft CQRS journey](https://github.com/MicrosoftArchive/cqrs-journey). 

For this project, I implemented simple inventory management system with 2 states: fresh and reserved (simple enough just for exploration)

Besides CQRS/ES, I also explore:

- Reactive progamming in general using RxJava
- Microservice concept like service discovery and different messaging infrastructure (Message Bus and HTTP)
- Domain-driven Design (DDD) concept like bounded context and aggregate root

	
## Tech stack used:
- [Vert.x](https://vertx.io) as the backbone for deployment and microservices
- [Kafka](https://kafka.apache.org/) as the message bus for command and event handler
- [EventStore](https://eventstore.org) for aggregate root and events storage
- [Akka](https://akka.io/) for EventStore client
- [MongoDB](https://www.mongodb.com/) for read model storage
	
## How to test

- Start kafka server (I used version 1.1.0 - scala 2.11) in port 9092 (default). Create 2 topic: CMD_TOPIC and EVT_TOPIC for command and event handler respectively
	
- download EventStore and start the server (https://eventstore.org/docs/introduction/index.html)
	
- run: 'mvn clean install' in the root folder
	
- run: 'java -jar ecommerce-1.0.0-SNAPSHOT-fat.jar' in target folder
	
- Available command (for now, you can test via postman or any webservice client)

   - POST http://localhost:8080/api/write/createNewInventory (field: name, desc, category, qty)
   
   - POST http://localhost:8080/api/write/markAsReserved (field: inventoryId, reservedBy)
   
   - POST http://localhost:8080/api/read/getFreshInventory 
   
   - POST http://localhost:8080/api/read/getReservedInventory 
	
- For debugging in eclipse, create a new Debug configuration. 
	
   - Main Class: io.vertx.core.Launcher 
   - Program Argument: run com.exploration.cqrs.ecommerce.MainVerticle
		

## TODO:

- [ ] Front end UI (Angular)

- [ ] 'Order' bounded context
	
- [ ] DockerFile for easy testing
	
- [ ] Architecture Documentation
	
- [ ] Try clustering?
	
- [ ] Benchmark the performance
