package com.exploration.cqrs.ecommerce.readmodel;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface InventoryDao {

	public Observable<FreshInventory> getAllInventory();

	public Single<FreshInventory> saveInventory(FreshInventory inventory);

	public Single<FreshInventory> removeInventory(String id);

	public Single<FreshInventory> findById(String id);

}
