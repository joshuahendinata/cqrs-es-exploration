package com.exploration.cqrs.ecommerce.readmodel;

import io.vertx.core.json.JsonObject;

public class FreshInventory extends JsonObject {
	/*
	 * private Long id; 
	 * private String name; 
	 * private String description; 
	 * private Double quantity; 
	 * private String category;
	 */
	
	public Long getId() {
		return this.getLong("_id");
	}

	public void setId(Long id) {
		this.put("_id", id);
	}

	public String getName() {
		return this.getString("name");
	}

	public void setName(String name) {
		this.put("name", name);
	}

	public String getDescription() {
		return this.getString("description");
	}

	public void setDescription(String description) {
		this.put("description", description);
	}

	public Double getQuantity() {
		return this.getDouble("quantity");
	}

	public void setQuantity(Double quantity) {
		this.put("quantity", quantity);
	}

	public String getCategory() {
		return this.getString("category");
	}

	public void setCategory(String category) {
		this.put("category", category);
	}

}
