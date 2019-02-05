package com.plugish.woominecraft.pojo;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Order {

	@SerializedName("player")
	@Expose
	private String player;
	@SerializedName("order_id")
	@Expose
	private Integer orderId;
	@SerializedName("commands")
	@Expose
	private List<String> commands = null;

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public Order withPlayer(String player) {
		this.player = player;
		return this;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Order withOrderId(Integer orderId) {
		this.orderId = orderId;
		return this;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public Order withCommands(List<String> commands) {
		this.commands = commands;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("player", player).append("orderId", orderId).append("commands", commands).toString();
	}

}