package org.onebusaway.nextbus.model;

import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("StopScheduleArrival")
public class StopScheduleArrival {
	
	@XStreamAlias("RouteID")
	private String routeId;
	
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

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

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

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
}
