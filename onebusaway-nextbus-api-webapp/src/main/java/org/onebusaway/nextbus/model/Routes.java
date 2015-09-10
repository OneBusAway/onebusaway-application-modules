package org.onebusaway.nextbus.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("RoutesResp")
public class Routes {
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns")
	final String xmlns = "http://www.wmata.com";
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns:i")
	final String xmlns_i = "http://www.w3.org/2001/XMLSchema-instance";
	
	@XStreamAlias("Routes")
	private List<Route> routes = new ArrayList<Route>();

	public List<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}
	
}
