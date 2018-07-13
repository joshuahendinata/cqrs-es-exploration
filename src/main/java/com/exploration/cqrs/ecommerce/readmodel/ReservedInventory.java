package com.exploration.cqrs.ecommerce.readmodel;

import java.util.Date;

public class ReservedInventory {
	private Long _id; 
	private String name; 
	private String description;
	private Double quantity;
	private String category;
	private String reservedBy;
	private Date reservedDate;
	public Long get_id() {
		return _id;
	}
	public void set_id(Long _id) {
		this._id = _id;
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
	public String getReservedBy() {
		return reservedBy;
	}
	public void setReservedBy(String reservedBy) {
		this.reservedBy = reservedBy;
	}
	public Date getReservedDate() {
		return reservedDate;
	}
	public void setReservedDate(Date reservedDate) {
		this.reservedDate = reservedDate;
	}
}
