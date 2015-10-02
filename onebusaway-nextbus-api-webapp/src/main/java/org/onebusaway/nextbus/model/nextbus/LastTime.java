package org.onebusaway.nextbus.model.nextbus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("lastTime")
public class LastTime {
	
	public LastTime(long time){
		this.time =  time;
	};
	
	@XStreamAsAttribute 
	private long time;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
