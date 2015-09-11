package org.onebusaway.nextbus.model;

import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("StopScheduleArrival")
public class StopScheduleArrival {
	
	@XStreamAlias("RouteID")
	private String routeId;
	
	@XStreamAlias("StartTime")
	private String startTime;
	
	@XStreamAlias("EndTime")
	private String endTime;
	
	@XStreamAlias("Time")
	private String time;
	
	@XStreamAlias("DirectionNum")
	private String directionNum;
	
	@XStreamAlias("TripDirectionText")
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

	public String getDirectionNum() {
		return directionNum;
	}

	public void setDirectionNum(String directionNum) {
		this.directionNum = directionNum;
	}
}
