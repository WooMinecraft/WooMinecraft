package com.plugish.woominecraft.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WMCPojo {

	@SerializedName("code")
	@Expose
	private String code;
	@SerializedName("message")
	@Expose
	private String message;
	@SerializedName("data")
	@Expose
	private Data data;
	@SerializedName("orders")
	@Expose
	private List<Order> orders = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public WMCPojo withCode(String code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public WMCPojo withMessage(String message) {
		this.message = message;
		return this;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public WMCPojo withData(Data data) {
		this.data = data;
		return this;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public WMCPojo withOrders(List<Order> orders) {
		this.orders = orders;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("code", code).append("message", message).append("data", data).append("orders", orders).toString();
	}

}