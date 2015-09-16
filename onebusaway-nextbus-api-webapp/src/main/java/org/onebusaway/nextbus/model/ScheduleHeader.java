package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("header")
public class ScheduleHeader {
	@XStreamImplicit
	private List<DisplayStop> stops = new ArrayList<DisplayStop>();

	public List<DisplayStop> getStops() {
		return stops;
	}

	public void setStops(List<DisplayStop> stops) {
		this.stops = stops;
	}
}
