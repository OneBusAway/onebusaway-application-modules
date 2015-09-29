package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.Direction;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.nextbus.model.nextbus.DisplayStop;
import org.onebusaway.nextbus.model.nextbus.Path;
import org.onebusaway.nextbus.model.nextbus.Point;
import org.onebusaway.nextbus.model.nextbus.Route;
import org.onebusaway.nextbus.model.nextbus.Stop;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RouteConfigAction implements ModelDriven<Body<Route>>{
	
	@Autowired
	private TransitDataService _service;
	
	private String agencyId;
	
	private String routeId;
	
	public String getA() {
		return agencyId;
	}
	
	@RequiredFieldValidator
	public void setA(String agencyId) {
		this.agencyId = agencyId;
	}
	
	public String getR() {
		return routeId;
	}

	public void setR(String routeId) {
		this.routeId = routeId;
	}

	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}
	
	@Override
	public Body<Route> getModel() {
		
		Body<Route> body = new Body<Route>();
		if(agencyId !=null){
			List<RouteBean> routeBeans = new ArrayList<RouteBean>();
			
			if(routeId != null){
				routeBeans.add(_service.getRouteForId(routeId));
			}
			else{
				routeBeans = _service.getRoutesForAgencyId(agencyId).getList();
			}
			
			for(RouteBean routeBean : routeBeans){
				Route route = new Route();
				route.setTag(routeBean.getId());
				route.setTitle(routeBean.getLongName());
				route.setShortTitle(routeBean.getShortName());
				route.setColor(routeBean.getColor());
				route.setOppositeColor(routeBean.getTextColor());
				
				StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);
				
				// Stops
				for(StopBean stopBean : stopsForRoute.getStops()){
					Stop stop = new Stop();
					stop.setTag(stopBean.getId());
					stop.setTitle(stopBean.getName());
					stop.setShortTitle(stopBean.getName());
					stop.setLat(stopBean.getLat());
					stop.setLon(stopBean.getLon());
					stop.setStopId(stopBean.getCode());
					route.getStops().add(stop);	
				}
				
				// Directions
				for(StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()){
					for(StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()){
						Direction direction = new Direction();
						direction.setTag(stopGroupBean.getId());
						direction.setTitle(stopGroupBean.getName().getName());
						for(String stopId: stopGroupBean.getStopIds()){
							direction.getStops().add(new DisplayStop(stopId));
						}
						route.getDirections().add(direction);
					}
				}
				
				// PolyLines
				for(EncodedPolylineBean polyline : stopsForRoute.getPolylines()){
					Path path = new Path();
					List<CoordinatePoint> coordinatePoints = PolylineEncoder.decode(polyline);
					for(CoordinatePoint coordinatePoint : coordinatePoints){
						path.getPoints().add(new Point(coordinatePoint.getLat(), coordinatePoint.getLon()));
					}
					route.getPaths().add(path);
				}
				
				body.getResponse().add(route);
			}
		}
		return body;
		
	}

}
