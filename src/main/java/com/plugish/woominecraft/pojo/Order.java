package com.plugish.woominecraft.pojo;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("order_id")
	@Expose
	private Integer orderId;
	@SerializedName("commands")
	@Expose
	private List<String> commands = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

}