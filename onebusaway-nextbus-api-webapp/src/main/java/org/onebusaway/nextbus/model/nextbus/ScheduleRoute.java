package org.onebusaway.nextbus.model.nextbus;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("route")
public class ScheduleRoute {
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String title;
	
	@XStreamAsAttribute 
	private String scheduleClass;
	
	@XStreamAsAttribute 
	private String serviceClass;
	
	@XStreamAsAttribute 
	private String direction;
	
	@XStreamAlias("header")
	private List<DisplayStop> stops = new ArrayList<DisplayStop>();
	
	@XStreamImplicit
	private List<ScheduleTableRow> scheduleTableRow = new ArrayList<ScheduleTableRow>();

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getScheduleClass() {
		return scheduleClass;
	}

	public void setScheduleClass(String scheduleClass) {
		this.scheduleClass = scheduleClass;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public List<DisplayStop> getStops() {
		return stops;
	}

	public void setStops(List<DisplayStop> stops) {
		this.stops = stops;
	}

	public List<ScheduleTableRow> getScheduleTableRow() {
		return scheduleTableRow;
	}

	public void setScheduleTableRow(List<ScheduleTableRow> scheduleTableRow) {
		this.scheduleTableRow = scheduleTableRow;
	}
}
