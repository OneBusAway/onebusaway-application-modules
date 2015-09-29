package org.onebusaway.nextbus.model.nextbus;

import java.math.BigDecimal;

import org.onebusaway.nextbus.impl.conversion.ListToStringConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("vehicle")
public class Vehicle {
	
	@XStreamAsAttribute 
	private String id;
	
	@XStreamAsAttribute 
	private String routeTag;
	
	@XStreamAsAttribute 
	private String dirTag;
	
	@XStreamAsAttribute
	private BigDecimal lat;
	
	@XStreamAsAttribute
	private BigDecimal lon;
	
	@XStreamAsAttribute
	private int secsSinceReport;
	
	@XStreamAsAttribute
	private boolean predictable;
	
	@XStreamAsAttribute
	private int heading;
	
	@XStreamAsAttribute
	private Double speedKmHr;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRouteTag() {
		return routeTag;
	}

	public void setRouteTag(String routeTag) {
		this.routeTag = routeTag;
	}

	public String getDirTag() {
		return dirTag;
	}

	public void setDirTag(String dirTag) {
		this.dirTag = dirTag;
	}

	public BigDecimal getLat() {
		return lat;
	}

	public void setLat(BigDecimal lat) {
		this.lat = lat;
	}

	public BigDecimal getLon() {
		return lon;
	}

	public void setLon(BigDecimal lon) {
		this.lon = lon;
	}

	public int getSecsSinceReport() {
		return secsSinceReport;
	}

	public void setSecsSinceReport(int secsSinceReport) {
		this.secsSinceReport = secsSinceReport;
	}

	public boolean getPredictable() {
		return predictable;
	}

	public void setPredictable(boolean predictable) {
		this.predictable = predictable;
	}

	public int getHeading() {
		return heading;
	}

	public void setHeading(int d) {
		this.heading = d;
	}

	public Double getSpeedKmHr() {
		return speedKmHr;
	}

	public void setSpeedKmHr(Double speedKmHr) {
		this.speedKmHr = speedKmHr;
	}
}
