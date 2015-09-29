package org.onebusaway.nextbus.model.nextbus;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("direction")
public class Direction {
	@XStreamImplicit
	private List<DisplayStop> stops = new ArrayList<DisplayStop>();
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String title;
	
	@XStreamAsAttribute 
	private String name;
	
	@XStreamAsAttribute 
	private boolean useForUI;
	
	public List<DisplayStop> getStops() {
		return stops;
	}

	public void setStops(List<DisplayStop> stops) {
		this.stops = stops;
	}

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

}
