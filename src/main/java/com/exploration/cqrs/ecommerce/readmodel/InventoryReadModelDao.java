package com.exploration.cqrs.ecommerce.readmodel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class InventoryReadModelDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryReadModelDao.class);

	private MongoClient client;
	
	public InventoryReadModelDao(MongoClient client) {
		this.client = client;
	}

	public void save(Object obj, String collection) {
		this.client.insert(collection, JsonObject.mapFrom(obj), res ->{
			if (res.succeeded()) {
				LOGGER.debug(obj.getClass().getSimpleName() + " saved successfully. ID:" + res.result());
			} else {
				LOGGER.error(res.cause().getMessage(), res.cause());
			}
		});
	}
	
	public Single<JsonObject> removeOneAndReturn(JsonObject query, String collection) {
		return this.client.rxFindOneAndDelete(collection, query);
	}
	
	public Single<List<JsonObject>> findReservedInventory() {
		FindOptions options = new FindOptions()
				.setLimit(10)
				.setSort(new JsonObject().put("_id",  1));
		
		return this.client
				.rxFindWithOptions("reservedInventory", new JsonObject(), options);	
	}
	
	public Single<List<JsonObject>> findFreshInventory() {
		FindOptions options = new FindOptions()
				.setLimit(10)
				.setSort(new JsonObject().put("_id",  1));
		
		return this.client
				.rxFindWithOptions("freshInventory", new JsonObject(), options);				
				/*.flatMapObservable(res -> {
					return Observable.fromIterable(res);
				})
				.map(json ->{
					
					FreshInventory result = new FreshInventory();
					json.forEach(entry -> {
						result.put(entry.getKey(), entry.getValue());
					});
					
					return result;
				})
				.toList();*/
	}
}
