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
package org.onebusaway.nextbus.model.transiTime;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("route")
public class ScheduleRoute {
	
	@XStreamAsAttribute 
	@XStreamAlias("tag")
	private String routeId;
	
	@XStreamAsAttribute
	@XStreamAlias("title")
	private String routeName;
	
	@XStreamAsAttribute
	@XStreamAlias("serviceClass")
	private String serviceName;
	
	@XStreamAsAttribute
	@XStreamAlias("direction")
	private String directionId;
	
	@XStreamAlias("header")
	private List<ScheduleHeader> stop = new ArrayList<ScheduleHeader>();
	
	@XStreamImplicit
	private List<ScheduleTableRow> timesForTrip = new ArrayList<ScheduleTableRow>();

	public List<ScheduleHeader> getStop() {
		return stop;
	}

	public void setStops(List<ScheduleHeader> stops) {
		this.stop = stop;
	}

	public List<ScheduleTableRow> getTimesForTrip() {
		return timesForTrip;
	}

	public void setTimesForTrip(List<ScheduleTableRow> timesForTrip) {
		this.timesForTrip = timesForTrip;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDirectionId() {
		return directionId;
	}

	public void setDirectionId(String directionId) {
		this.directionId = directionId;
	}

	public void setStop(List<ScheduleHeader> stop) {
		this.stop = stop;
	}
}
