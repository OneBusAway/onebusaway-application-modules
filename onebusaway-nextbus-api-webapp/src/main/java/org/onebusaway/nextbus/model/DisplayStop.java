package org.onebusaway.nextbus.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("stop")
public class DisplayStop {
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String value;
	
	public DisplayStop(){
	}
	
	public DisplayStop(String tag){
		this.tag = tag;
	}
	
	public DisplayStop(String tag, String value){
		this.tag = tag;
		this.value = value;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	

}
