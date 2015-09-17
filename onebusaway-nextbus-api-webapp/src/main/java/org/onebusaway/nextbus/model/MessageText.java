package org.onebusaway.nextbus.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("text")
public class MessageText {
	
	public MessageText(String value){
		this.value =  value;
	};
	
	@XStreamAsAttribute 
	private String value;

}
