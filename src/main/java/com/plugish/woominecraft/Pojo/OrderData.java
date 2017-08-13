package com.plugish.woominecraft.Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Jay on 8/11/2017.
 */
public class OrderData {

	@JsonProperty
	String player;

	@JsonProperty
	Integer orderID;

	@JsonProperty
	ArrayList<String> commands;

	@Override
	public String toString() {
		String listString = commands.stream().map( Object::toString ).collect( Collectors.joining(", ") );
		return "player = " + player + "; orderID = " + orderID + "; commands = " + listString;
	}
}
