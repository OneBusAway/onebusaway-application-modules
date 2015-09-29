package org.onebusaway.nextbus.model.nextbus;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("phonemeText")
public class MessagePhoneMeText {
	
	@XStreamAsAttribute 
	private String value;

}
