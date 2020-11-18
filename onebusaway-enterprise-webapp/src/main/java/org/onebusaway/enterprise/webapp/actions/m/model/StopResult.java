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
package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A stop as a top-level search result.
 * 
 * @author jmaki
 * 
 */
public class StopResult implements SearchResult {

	private StopBean stop;

	private List<RouteAtStop> routesWithArrivals;

	private List<RouteAtStop> routesWithNoVehiclesEnRoute;

	private List<RouteAtStop> routesWithNoScheduledService;
	
	private List<RouteBean> filteredRoutes;
	
	private Set<String> stopServiceAlerts;

	public StopResult(StopBean stop, List<RouteAtStop> routesWithArrivals, List<RouteAtStop> routesWithNoVehiclesEnRoute,
			List<RouteAtStop> routesWithNoScheduledService, List<RouteBean> filteredRoutes, Set<String> serviceAlerts) {
		this.stop = stop;
		this.routesWithArrivals = routesWithArrivals;
		this.routesWithNoVehiclesEnRoute = routesWithNoVehiclesEnRoute;
		this.routesWithNoScheduledService = routesWithNoScheduledService;
		this.filteredRoutes = filteredRoutes;
		this.stopServiceAlerts = serviceAlerts;
	}

	public String getId() {
		return stop.getId();
	}
	
	public String getCodeOrId() {
		if (stop.getCode() != null) {
			return stop.getCode();
		}
		return getId();
	}

	public String getIdWithoutAgency() {
		return AgencyAndIdLibrary.convertFromString(getId()).getId();
	}
	
	public String getCodeOrIdWithoutAgency() {
		if (stop.getCode() != null) {
			return stop.getCode();
		}
		return getIdWithoutAgency();
	}

	public String getName() {
		return stop.getName();
	}
	
	public String getCode() {
	  return stop.getCode();
	}

	public List<RouteAtStop> getAllRoutesAvailable() {
		List<RouteAtStop> fullList = new ArrayList<RouteAtStop>();
		fullList.addAll(routesWithArrivals);
		fullList.addAll(routesWithNoVehiclesEnRoute);
		fullList.addAll(routesWithNoVehiclesEnRoute);

		return fullList;
	}

	public List<RouteAtStop> getAllRoutesPossible() {
		List<RouteAtStop> fullList = new ArrayList<RouteAtStop>();
		fullList.addAll(routesWithArrivals);
		fullList.addAll(routesWithNoVehiclesEnRoute);
		fullList.addAll(routesWithNoVehiclesEnRoute);
		fullList.addAll(routesWithNoScheduledService);

		return fullList;
	}

	public List<RouteAtStop> getRoutesWithNoVehiclesEnRoute() {
		return routesWithNoVehiclesEnRoute;
	}

	public List<RouteAtStop> getRoutesWithNoScheduledService() {
		return routesWithNoScheduledService;
	}

	public List<RouteAtStop> getRoutesWithArrivals() {
		return routesWithArrivals;
	}
	
	public List<RouteBean> getFilteredRoutes() {
	  return filteredRoutes;
	}
	
	public Set<String> getStopServiceAlerts() {
		return stopServiceAlerts;
	}
}
