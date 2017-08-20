package com.plugish.woominecraft.Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Jay on 8/11/2017.
 */
public class OrderData {

	@JsonProperty
	String player;

	@JsonProperty
	Integer orderID;

	@JsonProperty(required = false)
	Boolean online = true;

	@JsonProperty
	ArrayList<String> commands;

	public OrderData() {}

	public String getPlayer() {
		return player;
	}

	public Integer getOrderID() {
		return orderID;
	}

	public Boolean getOnline(){
		return online;
	}

	public ArrayList<String> getCommands() {
		return commands;
	}
}
