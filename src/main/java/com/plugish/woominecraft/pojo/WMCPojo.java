package com.plugish.woominecraft.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WMCPojo {

	@SerializedName("orders")
	@Expose
	private List<Order> orders = null;

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

}