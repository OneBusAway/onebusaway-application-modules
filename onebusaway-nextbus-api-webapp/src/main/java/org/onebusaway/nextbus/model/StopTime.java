package org.onebusaway.nextbus.model;

import java.util.Date;

import com.opensymphony.xwork2.conversion.annotations.Conversion;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("StopTime")
@Conversion()
public class StopTime {
	
	@XStreamAlias("StopID")
	private String stopId = "0";
	
	@XStreamAlias("StopName")
	private String stopName;
	
	@XStreamAlias("StopSeq")
	private int stopSeq;
	
	@XStreamAlias("Time")
	private String time;
	
	public String getStopId() {
		return stopId;
	}
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	public String getStopName() {
		return stopName;
	}
	public void setStopName(String stopName) {
		this.stopName = stopName;
	}
	public int getStopSeq() {
		return stopSeq;
	}
	public void setStopSeq(int stopSeq) {
		this.stopSeq = stopSeq;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
}
