package org.onebusaway.nextbus.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Route")
public class Route {
	@XStreamAlias("RouteID")
	private String routeId;
	
	@XStreamAlias("Name")
	private String routeName;
	
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getName() {
		return routeName;
	}
	public void setName(String routeName) {
		this.routeName = routeName;
	}
}
