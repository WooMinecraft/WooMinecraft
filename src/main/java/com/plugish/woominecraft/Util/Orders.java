package com.plugish.woominecraft.Util;

import com.plugish.woominecraft.Pojo.CompletedOrders;
import com.plugish.woominecraft.Pojo.OrderResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Created by Jay on 8/13/2017.
 */
public class Orders {

	public OrderResponse getAllOrders( String Server ) throws Exception {
		Client client = ClientBuilder.newClient();
		Response response = client.target( Server ).request().get();

		OrderResponse orderResponses = response.readEntity( OrderResponse.class );
		client.close();
		return orderResponses;
	}

	public Integer updateOrders( String Server, ArrayList<Integer> orders ) throws Exception {
		Client client = ClientBuilder.newClient();
		CompletedOrders completedOrders = new CompletedOrders( orders );

		Response response = client.target(Server).request().post( Entity.json( completedOrders ) );
		client.close();

		return response.getStatus();
	}
}
