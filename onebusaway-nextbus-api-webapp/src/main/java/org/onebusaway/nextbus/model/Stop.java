package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Stop")
public class Stop {
	
	@XStreamAlias("StopId")
	private String stopId = "0";
	
	@XStreamAlias("StopName")
	private String stopName;
	
	@XStreamAlias("Lat")
	private double lat;
	
	@XStreamAlias("Lon")
	private double lon;
	
	@XStreamAlias("Routes")
	private List<String> routes = new ArrayList<String>();
	
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	public String getStopName() {
		return stopName;
	}
	public void setStopName(String stopName) {
		this.stopName = stopName;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double d) {
		this.lat = d;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double d) {
		this.lon = d;
	}
	public List<String> getRoutes() {
		return routes;
	}
	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}
	
}
