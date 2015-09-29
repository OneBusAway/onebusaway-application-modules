package org.onebusaway.nextbus.model.nextbus;

import java.util.Set;

import org.onebusaway.transit_data.model.trips.TripDetailsBean;


public class ScheduleResult {
	
	private Set<TripDetailsBean> tripDetails;
	
	private String directionId;
	
	private String blockId;

	public Set<TripDetailsBean> getTripDetails() {
		return tripDetails;
	}

	public void setTripDetails(Set<TripDetailsBean> tripDetails) {
		this.tripDetails = tripDetails;
	}

	public String getDirectionId() {
		return directionId;
	}

	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	
}
