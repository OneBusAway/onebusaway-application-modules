package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("route")
public class Route {
	@XStreamImplicit
	private List<Stop> stops = new ArrayList<Stop>();
	
	@XStreamImplicit
	private List<Direction> directions = new ArrayList<Direction>();
	
	@XStreamImplicit
	private List<Path> paths = new ArrayList<Path>();
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String title;
	
	@XStreamAsAttribute 
	private String shortTitle;
	
	@XStreamAsAttribute 
	private String color;
	
	@XStreamAsAttribute 
	private String oppositeColor;
	
	@XStreamAsAttribute 
	private String latMin;
	
	@XStreamAsAttribute 
	private String latMax;
	
	@XStreamAsAttribute 
	private String lonMin;
	
	@XStreamAsAttribute 
	private String lonMax;

	public List<Stop> getStops() {
		return stops;
	}

	public void setStops(List<Stop> stops) {
		this.stops = stops;
	}

	public List<Direction> getDirections() {
		return directions;
	}

	public void setDirections(List<Direction> directions) {
		this.directions = directions;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
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

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getOppositeColor() {
		return oppositeColor;
	}

	public void setOppositeColor(String oppositeColor) {
		this.oppositeColor = oppositeColor;
	}

	public String getLatMin() {
		return latMin;
	}

	public void setLatMin(String latMin) {
		this.latMin = latMin;
	}

	public String getLatMax() {
		return latMax;
	}

	public void setLatMax(String latMax) {
		this.latMax = latMax;
	}

	public String getLonMin() {
		return lonMin;
	}

	public void setLonMin(String lonMin) {
		this.lonMin = lonMin;
	}

	public String getLonMax() {
		return lonMax;
	}

	public void setLonMax(String lonMax) {
		this.lonMax = lonMax;
	}
	
}
