package com.exploration.cqrs.ecommerce.boundedcontext;

import java.io.Serializable;

public class InventoryContext implements BoundedContext, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4235829337350879988L;
	private Long id;
	private String name;
	private String description;
	private Double quantity;
	private String category;
	private String status;
	private Double soldQuantity = 0.0;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getSoldQuantity() {
		return soldQuantity;
	}

	public void setSoldQuantity(Double soldQuantity) {
		this.soldQuantity = soldQuantity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
