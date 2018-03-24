/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api.model;

import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.StopBean;

import java.util.List;

/**
 * A stop as a top-level search result.
 * 
 * @author jmaki
 * 
 */
public class StopResult implements SearchResult {

	private StopBean stop;

	private List<RouteAtStop> routesAvailable;

	public StopResult(StopBean stop, List<RouteAtStop> routesAvailable) {
		this.stop = stop;
		this.routesAvailable = routesAvailable;
	}
	
	public String toString() {
	  return "{stop=" + stop 
	      + ", routesAvailable=" + routesAvailable
	      + "}";
	}

	public String getId() {
		return stop.getId();
	}
	
	public String getCode() {
		return stop.getCode();
	}

	public String getName() {
		return stop.getName();
	}

	public Double getLatitude() {
		return stop.getLat();
	}

	public Double getLongitude() {
		return stop.getLon();
	}

	public String getStopDirection() {
		if (stop.getDirection() == null || (stop.getDirection() != null && stop.getDirection().equals("?"))) {
			return "unknown";
		} else {
			return stop.getDirection();
		}
	}

	public List<RouteAtStop> getRoutesAvailable() {
		return routesAvailable;
	}
}
