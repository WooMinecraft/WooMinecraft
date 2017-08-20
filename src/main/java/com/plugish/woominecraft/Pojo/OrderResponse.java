package com.plugish.woominecraft.Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Jay on 7/22/2017.
 */
public class OrderResponse {

	@JsonProperty
	private String serverKey;

	@JsonProperty
	private ArrayList<OrderData> orderData;

	public OrderResponse(){}

	/**
	 * Gets the server key.
	 * @return String
	 */
	public String getServerKey() {
		return serverKey;
	}

	/**
	 * Gets the order data.
	 * @return ArrayList<OrderData>
	 */
	public ArrayList<OrderData> getOrderData() {
		return orderData;
	}

	/**
	 * Gets a specific order ID.
	 * @param id The Order ID to look for.
	 * @return OrderData An orderData object.
	 * @throws Exception
	 */
	public OrderData getOrder( Integer id ) throws Exception {
		for ( OrderData o : orderData ) {
			if ( o.orderID.equals( id ) ) {
				return o;
			}
		}

		throw new Exception( "No order ID available for order: " + id.toString() );
	}

	/**
	 * Grabs all orders by a specific player ID.
	 * @param playerName The string of the player name, case doesn't matter.
	 * @return ArrayList<OrderData> Returns an array of orders.
	 * @throws Exception If no order is found by player id.
	 */
	public ArrayList<OrderData> getOrdersByPlayer( String playerName ) throws Exception {

		ArrayList<OrderData> _orderData = new ArrayList<>();
		for ( OrderData o : orderData ) {
			if ( o.player.equalsIgnoreCase( playerName ) ) {
				_orderData.add( o );
			}
		}

		if ( _orderData.isEmpty() ) {
			throw new Exception( "No orders by player " + playerName );
		}

		return _orderData;
	}

}
