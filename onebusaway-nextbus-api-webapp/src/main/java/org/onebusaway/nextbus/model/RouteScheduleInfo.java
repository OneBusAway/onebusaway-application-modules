package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("RouteScheduleInfo")
public class RouteScheduleInfo {
	
	@XStreamAlias("Direction0")
	List<Trip> direction0 = new ArrayList<Trip>();
	
	@XStreamAlias("Direction1")
	List<Trip> direction1 = new ArrayList<Trip>();
	
	public List<Trip> getDirection0() {
		return direction0;
	}
	public void setDirection0(List<Trip> direction0) {
		this.direction0 = direction0;
	}
	public List<Trip> getDirection1() {
		return direction1;
	}
	public void setDirection1(List<Trip> direction1) {
		this.direction1 = direction1;
	}
}
