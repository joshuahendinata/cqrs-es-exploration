package com.exploration.cqrs.ecommerce.readmodel.implementation;

import com.exploration.cqrs.ecommerce.readmodel.InventoryDao;
import com.exploration.cqrs.ecommerce.readmodel.FreshInventory;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class MongoClientInventoryDao implements InventoryDao {

	private MongoClient client;
	
	public MongoClientInventoryDao(MongoClient client) {
		this.client = client;
	}

	@Override
	public Observable<FreshInventory> getAllInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Single<FreshInventory> saveInventory(FreshInventory inventory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Single<FreshInventory> removeInventory(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Single<FreshInventory> findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
