package org.onebusaway.nextbus.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("agency")
public class Agency {
	
	@XStreamAsAttribute 
	private String tag;
	
	@XStreamAsAttribute 
	private String title;
	
	@XStreamAsAttribute 
	private String shortTitle;
	
	@XStreamAsAttribute 
	private String regionTitle;

	public String getRegionTitle() {
		return regionTitle;
	}

	public void setRegionTitle(String regionTitle) {
		this.regionTitle = regionTitle;
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
	
}
