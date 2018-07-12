package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.boundedcontext.BoundedContext;

import io.reactivex.Single;

public interface WriteModelRepoSvc<T extends BoundedContext> {

	public Single<T> findById(Long id);
	
	public void save(T objectToBeSaved);
}
