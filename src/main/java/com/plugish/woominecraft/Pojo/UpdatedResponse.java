package com.plugish.woominecraft.Pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Jay on 8/13/2017.
 */
public class UpdatedResponse {

	@JsonProperty
	String message;

	@JsonProperty
	ArrayList<Integer> skipped;

	@JsonProperty
	ArrayList<Integer> processed;

	public UpdatedResponse(){}
}
