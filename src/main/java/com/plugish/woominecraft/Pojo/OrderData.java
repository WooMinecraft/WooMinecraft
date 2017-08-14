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
	String online;

	@JsonProperty
	ArrayList<String> commands;
}
