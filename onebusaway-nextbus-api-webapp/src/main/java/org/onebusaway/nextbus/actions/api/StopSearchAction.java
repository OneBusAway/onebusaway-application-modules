package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.nextbus.impl.ConversionUtil;
import org.onebusaway.nextbus.model.Stop;
import org.onebusaway.nextbus.model.StopsResp;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class StopSearchAction implements ModelDriven<StopsResp> {
	
	@Autowired
	private TransitDataService service;
	
	private double radius;
	
	private double lat;
	
	private double lon;
	
	private static final double DEFAULT_SEARCH_RADIUS = 500;
	
	public TransitDataService get_service() {
		return service;
	}

	public void set_service(TransitDataService service) {
		this.service = service;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public StopsResp getModel() {
		StopsResp stops = new StopsResp();
		List<StopBean> stopBeans = new ArrayList<StopBean>();
		
	    SearchQueryBean searchQuery = new SearchQueryBean();
	    searchQuery.setBounds(getSearchBounds());
	    searchQuery.setMaxCount(Integer.MAX_VALUE);
	    searchQuery.setType(EQueryType.BOUNDS);

	    try {
	      StopsBean stopsBean = service.getStops(searchQuery);
	      stopBeans = stopsBean.getStops();
	    } catch (OutOfServiceAreaServiceException ex) {
	    	// TODO - See how this is handled
	    }
	    
	    for(StopBean stopBean : stopBeans){
	    	Stop stop = new Stop();
	    	
	    	stop.setStopId(stopBean.getId());
	    	stop.setStopName(stopBean.getName());
	    	stop.setLat(stopBean.getLat());
	    	stop.setLon(stopBean.getLon());
	    	
	    	for(RouteBean routeBean : stopBean.getRoutes()){
	    		stop.getRoutes().add(routeBean.getId());
	    	}
	    	
	    	stops.getStops().add(stop);
	    }
		
		return stops;
		
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = ConversionUtil.footToMeter(radius);
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}
	
	private CoordinateBounds getSearchBounds() {
	    if (radius > 0) {
	      return SphericalGeometryLibrary.bounds(lat, lon, radius);
	    } 
	    return SphericalGeometryLibrary.bounds(lat, lon, DEFAULT_SEARCH_RADIUS);
	}

}
