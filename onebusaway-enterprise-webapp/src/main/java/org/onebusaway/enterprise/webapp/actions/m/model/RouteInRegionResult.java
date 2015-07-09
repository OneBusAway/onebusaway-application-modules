package org.onebusaway.enterprise.webapp.actions.m.model;

import org.onebusaway.nyc.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.RouteBean;

/**
 * Route available near or within an area.
 * 
 * @author jmaki
 * 
 */
public class RouteInRegionResult implements SearchResult {

	private RouteBean route;

	public RouteInRegionResult(RouteBean route) {
		this.route = route;
	}

	public String getShortName() {
		return route.getShortName();
	}
}
