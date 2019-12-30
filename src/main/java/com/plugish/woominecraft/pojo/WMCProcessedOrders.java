package com.plugish.woominecraft.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WMCProcessedOrders {

	@SerializedName("processedOrders")
	@Expose
	private List<Integer> processedOrders = null;

	public List<Integer> getProcessedOrders() {
		return processedOrders;
	}

	public void setProcessedOrders(List<Integer> processedOrders) {
		this.processedOrders = processedOrders;
	}

	public WMCProcessedOrders withProcessedOrders(List<Integer> processedOrders) {
		this.processedOrders = processedOrders;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("processedOrders", processedOrders).toString();
	}

}