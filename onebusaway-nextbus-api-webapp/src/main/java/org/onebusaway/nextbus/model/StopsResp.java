package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("StopsResp")
public class StopsResp {
	
	@XStreamAlias("Stops")
	private List<Stop> stops = new ArrayList<Stop>();

	public List<Stop> getStops() {
		return stops;
	}

	public void setStops(List<Stop> stopId) {
		this.stops = stopId;
	}
}
