package org.onebusaway.nextbus.model.nextbus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("interval")
public class MessageInterval {
	
	@XStreamAsAttribute 
	private int startDay;
	
	@XStreamAsAttribute 
	private long startTime;
	
	@XStreamAsAttribute 
	private int endDay;
	
	@XStreamAsAttribute 
	private long  endTime;

	public int getStartDay() {
		return startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getEndDay() {
		return endDay;
	}

	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
