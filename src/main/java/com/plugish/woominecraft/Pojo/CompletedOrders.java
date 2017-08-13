package com.plugish.woominecraft.Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Jay on 8/13/2017.
 */
public class CompletedOrders {

	@JsonProperty
	private ArrayList<Integer> Orders = new ArrayList<>();

	public void addOrder( Integer id ) {
		if ( !Orders.contains( id ) ) {
			Orders.add( id );
		}
	}
}
