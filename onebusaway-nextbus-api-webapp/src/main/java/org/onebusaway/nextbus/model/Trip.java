package org.onebusaway.nextbus.model;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Trip")
public class Trip {

	@XStreamAlias("StartTime")
	private Calendar startTime;
	
	@XStreamAlias("EndTime")
	private Calendar endTime;
	
	@XStreamAlias("Time")
	private Calendar time;
	
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
	
	
	public Calendar getStartTime() {
		return startTime;
	}
	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}
	public Calendar getEndTime() {
		return endTime;
	}
	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}
	public Calendar getTime() {
		return time;
	}
	public void setTime(Calendar time) {
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
