package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("predictions")
public class Predictions {
	
	@XStreamAsAttribute 
	private String agencyTitle;
	
	@XStreamAsAttribute 
	private String routeTag;
	
	@XStreamAsAttribute 
	private String routeTitle;
	
	@XStreamAsAttribute 
	private String stopTitle;
	
	@XStreamAsAttribute 
	private String dirTitleBecauseNoPredictions;
	
	@XStreamAsAttribute 
	private boolean isDeparture;
	
	@XStreamAsAttribute 
	private String block;
	
	@XStreamAsAttribute 
	private String dirTag;
	
	@XStreamAsAttribute 
	private String tripTag;
	
	@XStreamAsAttribute 
	private String branch;
	
	@XStreamAsAttribute 
	private Boolean affectedByLayover;
	
	@XStreamAsAttribute 
	private Boolean isScheduledBased;
	
	@XStreamAsAttribute 
	private Boolean delayed;
}
