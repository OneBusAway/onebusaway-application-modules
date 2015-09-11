package org.onebusaway.nextbus.model;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Trip")
public class Trip {

	@XStreamAlias("StartTime")
	private String startTime;
	
	@XStreamAlias("EndTime")
	private String endTime;
	
	@XStreamAlias("Time")
	private String time;
	
	@XStreamAlias("DirectionNum")
	private String tripDirectionText;
	
	@XStreamAlias("TripHeadsign")
	private String tripHeadsign;
	
	@XStreamAlias("TripID")
	private String tripId;
	
	@XStreamAlias("RouteID")
	private String routeId;
	
	@XStreamAlias("StopTimes")
	private List<StopTime> stopTimes = new ArrayList<StopTime>();
	
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<StopTime> getStopTimes() {
		return stopTimes;
	}
	public void setStopTimes(List<StopTime> stopTimes) {
		this.stopTimes = stopTimes;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getTripDirectionText() {
		return tripDirectionText;
	}
	public void setTripDirectionText(String tripDirectionText) {
		this.tripDirectionText = tripDirectionText;
	}
	public String getTripHeadsign() {
		return tripHeadsign;
	}
	public void setTripHeadsign(String tripHeadsign) {
		this.tripHeadsign = tripHeadsign;
	}
	public String getTripID() {
		return tripId;
	}
	public void setTripID(String tripId) {
		this.tripId = tripId;
	}
}
