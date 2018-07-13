package com.exploration.cqrs.ecommerce.boundedcontext;

import java.io.Serializable;
import java.util.Date;

import com.exploration.cqrs.ecommerce.command.MarkAsReserved;
import com.exploration.cqrs.ecommerce.event.InventoryRegistered;
import com.exploration.cqrs.ecommerce.event.InventoryReserved;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class InventoryContext extends EventSourcedBoundedContext implements Serializable {

	@JsonIgnore
	private static final long serialVersionUID = -4235829337350879988L;
	
	private Long id;
	private String name;
	private String description;
	private Double quantity;
	private String category;
	private String status;
	private Double soldQuantity = 0.0;
	private String reservedBy;
	private Date reserveDate;

	public InventoryContext() {}
	
	public InventoryContext(Long id, String name, String description, 
			Double quantity, String category) {
		//super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
		this.category = category;
		this.setVersion(1);
		
		InventoryRegistered invRegisteredEvt = new InventoryRegistered();
		invRegisteredEvt.setSourceId(this.id);
		invRegisteredEvt.setInventoryName(this.name);
		invRegisteredEvt.setInventoryDescription(this.description);
		invRegisteredEvt.setInventoryCategory(this.category);
		invRegisteredEvt.setInventoryQuantity(this.quantity);
		invRegisteredEvt.setVersion(this.getVersion());
		this.update(invRegisteredEvt);
	}
	
	@Override
	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	private void setDescription(String description) {
		this.description = description;
	}

	public Double getQuantity() {
		return quantity;
	}

	private void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public String getCategory() {
		return category;
	}

	private void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	private void setStatus(String status) {
		this.status = status;
	}

	public Double getSoldQuantity() {
		return soldQuantity;
	}

	private void setSoldQuantity(Double soldQuantity) {
		this.soldQuantity = soldQuantity;
	}

	@Override
	public void onEvent(InventoryRegistered e) {
		this.setId(e.getSourceId());
		this.setName(e.getInventoryName());
		this.setDescription(e.getInventoryDescription());
		this.setQuantity(e.getInventoryQuantity());
		this.setCategory(e.getInventoryCategory());
		this.setVersion(e.getVersion());
		this.setStatus("FreshInventory");
	}
	
	@Override
	public void onEvent(InventoryReserved e) {
		this.status = "Reserved";
		this.reservedBy = e.getReservedBy();
	}

	public void markAsReserved(MarkAsReserved command) {
		InventoryReserved e = new InventoryReserved();
		e.setSourceId(command.getInventoryId());
		e.setReservedBy(command.getReservedBy());
		e.setReservedDate(new Date());
		e.setVersion(this.getVersion() + 1);
		this.update(e);
	}

}
