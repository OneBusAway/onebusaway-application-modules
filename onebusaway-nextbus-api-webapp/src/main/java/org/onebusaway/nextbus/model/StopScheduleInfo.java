package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("StopScheduleInfo")
public class StopScheduleInfo {
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns")
	final String xmlns = "http://www.wmata.com";
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns:i")
	final String xmlns_i = "http://www.w3.org/2001/XMLSchema-instance";
	
	@XStreamAlias("ScheduleArrivals")
	List<StopScheduleArrival> scheduleArrivals = new ArrayList<StopScheduleArrival>();
	
	@XStreamAlias("Stop")
	Stop stop;

	
	public List<StopScheduleArrival> getScheduleArrivals() {
		return scheduleArrivals;
	}

	public void setScheduleArrivals(List<StopScheduleArrival> scheduleArrivals) {
		this.scheduleArrivals = scheduleArrivals;
	}

	public Stop getStop() {
		return stop;
	}

	public void setStop(Stop stop) {
		this.stop = stop;
	}
	
}
