package com.exploration.cqrs.ecommerce.command;

import java.io.Serializable;

import com.exploration.cqrs.ecommerce.handler.CommandHandler;

public class RegisterNewInventory implements Command, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8089067902657315920L;
	private Long id;
	private String name;
	private String desc;
	private String category;
	private Double qty;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setCommandId(Long id) {
		this.id = id;
	}

	@Override
	public Long getCommandId() {
		return this.id;
	}

	@Override
	public void acceptHandler(CommandHandler handler)  {
		handler.handle(this);
	}

}
