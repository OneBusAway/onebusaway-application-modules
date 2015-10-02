/**
 * Copyright (C) 2015 Cambridge Systematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
