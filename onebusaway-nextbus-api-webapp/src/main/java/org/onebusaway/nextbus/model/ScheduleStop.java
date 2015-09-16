package org.onebusaway.nextbus.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("stop")
public class ScheduleStop {
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String value;
	
	@XStreamAsAttribute 
	private long epochTime;
	
	@XStreamOmitField
	private String stopName;
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getEpochTime() {
		return epochTime;
	}

	public void setEpochTime(long epochTime) {
		this.epochTime = epochTime;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}
	
	@Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((tag == null) ? 0 : tag.hashCode());
      return result;
    }
	
	@Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      
      ScheduleStop other = (ScheduleStop) obj;
      if(this.getTag().equals(other.getTag()))
    	  return true;
      return false;
    }
	

}
