package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.impl.DateUtil;
import org.onebusaway.nextbus.model.Agency;
import org.onebusaway.nextbus.model.Body;
import org.onebusaway.nextbus.model.DisplayStop;
import org.onebusaway.nextbus.model.ScheduleResult;
import org.onebusaway.nextbus.model.ScheduleRoute;
import org.onebusaway.nextbus.model.ScheduleStop;
import org.onebusaway.nextbus.model.ScheduleTableRow;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ScheduleAction implements ModelDriven<Body<ScheduleRoute>> {
	@Autowired
	private TransitDataService _service;
	
	private static int MINUTES_IN_DAY = 1440;
	
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
	@RequiredFieldValidator
	public void setR(String routeId) {
		this.routeId = routeId;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}
	
	@Override
	public Body<ScheduleRoute> getModel() {
		
		Body<ScheduleRoute> body = new Body<ScheduleRoute>();
		if(agencyId !=null){
			
			
			
			List<HashMap<String, HashSet<ScheduleStop>>> blockStopsMapList = new ArrayList<HashMap<String, HashSet<ScheduleStop>>>();
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			
			//Get All the Stops for a specific route
			StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);
			
			
			for(StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()){
				for(StopGroupBean stopGroupBean: stopGroupingBean.getStopGroups()){
					
					// Weekday Trips
					for(Long weekdayTime : DateUtil.getWeekdayDateTimes()){
						ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
						query.setTime(weekdayTime);
						query.setMinutesBefore(0);
						query.setMinutesAfter(MINUTES_IN_DAY);
						
						StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
						
						for(ArrivalAndDepartureBean arrivalsAndDeparture : stopsWithArrivals.getArrivalsAndDepartures()){
							
							// Filter Arrivals and Departures By Route
							if(arrivalsAndDeparture.getTrip().getRoute().getId().equals(routeId)){

								ScheduleStop scheduleStop = new ScheduleStop();
								scheduleStop.setTag(arrivalsAndDeparture.getStop().getId());
								scheduleStop.setEpochTime(arrivalsAndDeparture.getScheduledArrivalTime());
								scheduleStop.setStopName(arrivalsAndDeparture.getStop().getName());

								if(arrivalsAndDeparture.getTrip().getDirectionId().equals("0")){
									addStopByBlockId(blockStopsMapList.get(0), arrivalsAndDeparture.getTrip().getBlockId(), scheduleStop);
								}
								else if(arrivalsAndDeparture.getTrip().getDirectionId().equals("1")){
									addStopByBlockId(blockStopsMapList.get(1), arrivalsAndDeparture.getTrip().getBlockId(), scheduleStop);
								}
							}
						}
					}
					
					// Weekend Trips
					for(Long weekendTime : DateUtil.getWeekendDateTimes()){
						ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
						query.setTime(weekendTime);
						query.setMinutesBefore(0);
						query.setMinutesAfter(MINUTES_IN_DAY);
						
						StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
						
						for(ArrivalAndDepartureBean arrivalsAndDeparture : stopsWithArrivals.getArrivalsAndDepartures()){
							
							// Filter Arrivals and Departures By Route
							if(arrivalsAndDeparture.getTrip().getRoute().getId().equals(routeId)){

								ScheduleStop scheduleStop = new ScheduleStop();
								scheduleStop.setTag(arrivalsAndDeparture.getStop().getId());
								scheduleStop.setEpochTime(arrivalsAndDeparture.getScheduledArrivalTime());
								scheduleStop.setStopName(arrivalsAndDeparture.getStop().getName());
								
								if(arrivalsAndDeparture.getTrip().getDirectionId().equals("0")){
									addStopByBlockId(blockStopsMapList.get(2), arrivalsAndDeparture.getTrip().getBlockId(), scheduleStop);
								}
								else if(arrivalsAndDeparture.getTrip().getDirectionId().equals("1")){
									addStopByBlockId(blockStopsMapList.get(3), arrivalsAndDeparture.getTrip().getBlockId(), scheduleStop);
								}
							}
						}
					}
				}
			}
			
			// Routes
			for(int n=0; n < blockStopsMapList.size(); n++){
				HashMap<String, HashSet<ScheduleStop>> blockStopsMap = blockStopsMapList.get(n);
				ScheduleRoute scheduleRoute = new ScheduleRoute();
				scheduleRoute.setDirection(Integer.toString(n%2));
				scheduleRoute.setTag(stopsForRoute.getRoute().getId());
				scheduleRoute.setServiceClass(n < 3 ? "wkd" : "wkend");
		
				// Blocks
				for (Entry<String, HashSet<ScheduleStop>> entry : blockStopsMap.entrySet()) {
					int tripStopTimeCounter = 0;
				    String blockId = entry.getKey();
				    HashSet<ScheduleStop> blockStops = entry.getValue();
				    
				    ScheduleTableRow scheduleTr = new ScheduleTableRow(blockId);
				    
				    // Stop Times
				    for(ScheduleStop stop : blockStops){
				    	if(tripStopTimeCounter == 0){
				    		DisplayStop displayStop = new DisplayStop();
				    		displayStop.setTag(stop.getTag());
				    		displayStop.setValue(stop.getStopName());
				    		scheduleRoute.getStops().add(displayStop);	
				    	}				    	
				    	//scheduleStop.setValue(value);
				    	scheduleTr.getStops().add(stop);
				    }
				    scheduleRoute.getScheduleTableRow().add(scheduleTr);
				}
				body.getResponse().add(scheduleRoute);
				
			}
			
		}
		return body;	
	}
	
	private void addStopByBlockId(Map<String, HashSet<ScheduleStop>> stopsByBlockIds, 
			String blockId, ScheduleStop scheduleStop){
		
		if(!stopsByBlockIds.containsKey(blockId)){
			stopsByBlockIds.put(blockId, new HashSet<ScheduleStop>());
		}
		stopsByBlockIds.get(blockId).add(scheduleStop);
	}
}
