package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.nyc.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.RouteBean;

import java.util.List;
import java.util.Set;

/**
 * Route as a top-level search result.
 * 
 * @author jmaki
 * 
 */
public class RouteResult implements SearchResult {

	private RouteBean route;

	private List<RouteDirection> directions;

	private Set<String> serviceAlerts;

	public RouteResult(RouteBean route, List<RouteDirection> directions, Set<String> serviceAlerts) {
		this.route = route;
		this.directions = directions;
		this.serviceAlerts = serviceAlerts;
	}

	public String getId() {
		return route.getId();
	}

	public String getShortName() {
		return route.getShortName();
	}

	public String getLongName() {
		return route.getLongName();
	}

	public String getDescription() {
		return route.getDescription();
	}

	public String getColor() {
		if (route.getColor() != null) {
			return route.getColor();
		} else {
			return "000000";
		}
	}

	public Set<String> getServiceAlerts() {
		return serviceAlerts;
	}

	public List<RouteDirection> getDirections() {
		return directions;
	}
}
