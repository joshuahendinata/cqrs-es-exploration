package com.exploration.cqrs.ecommerce.handler;

import com.exploration.cqrs.ecommerce.boundedcontext.BoundedContext;

public interface WriteModelRepoSvc<T extends BoundedContext> {

	public T findById(Long id);
	
	public void save(T objectToBeSaved);
}
