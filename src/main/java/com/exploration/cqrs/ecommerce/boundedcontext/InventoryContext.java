 package com.exploration.cqrs.ecommerce.boundedcontext;

public class InventoryContext {

	private String id;
	private String name;
	private String description;
	private Integer quantity;
	private String category;
	private Integer soldQuantity;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Integer getSoldQuantity() {
		return soldQuantity;
	}
	public void setSoldQuantity(Integer soldQuantity) {
		this.soldQuantity = soldQuantity;
	}
	
	
}
