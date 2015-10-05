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
package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.util.DateUtil;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.DisplayStop;
import org.onebusaway.nextbus.model.nextbus.ScheduleRoute;
import org.onebusaway.nextbus.model.nextbus.ScheduleStop;
import org.onebusaway.nextbus.model.nextbus.ScheduleTableRow;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class ScheduleAction extends NextBusApiBase implements ModelDriven<Body<ScheduleRoute>> {
	@Autowired
	private TransitDataService _service;
	
	private static int MINUTES_IN_DAY = 1440;
	
	private String agencyId;
	
	private String routeId;
	
	public String getA() {
		return agencyId;
	}
	

	public void setA(String agencyId) {
		this.agencyId = getMappedAgency(agencyId);
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
	public Body<ScheduleRoute> getModel() {
		
		Body<ScheduleRoute> body = new Body<ScheduleRoute>();
		List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
		
		if(this.isValid(body, routeIds)){
			
			AgencyBean agency = _transitDataService.getAgency(agencyId);
			
			List<HashMap<String, HashSet<ScheduleStop>>> blockStopsMapList = new ArrayList<HashMap<String, HashSet<ScheduleStop>>>();
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			blockStopsMapList.add(new HashMap<String, HashSet<ScheduleStop>>());
			
			//Get All the Stops for a specific route
			for(AgencyAndId routeId : routeIds){		
				String route = AgencyAndId.convertToString(routeId);
				StopsForRouteBean stopsForRoute = _service.getStopsForRoute(route);
				
				for(StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()){
					for(StopGroupBean stopGroupBean: stopGroupingBean.getStopGroups()){
						
						// Weekday Trips
						for(Long weekdayTime : DateUtil.getWeekdayDateTimes(agency.getTimezone())){
							ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
							query.setTime(weekdayTime);
							query.setMinutesBefore(0);
							query.setMinutesAfter(MINUTES_IN_DAY);
							
							StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
							
							for(ArrivalAndDepartureBean arrivalsAndDeparture : stopsWithArrivals.getArrivalsAndDepartures()){
								
								// Filter Arrivals and Departures By Route
								if(arrivalsAndDeparture.getTrip().getRoute().getId().equals(route)){
	
									ScheduleStop scheduleStop = new ScheduleStop();
									scheduleStop.setTag(getIdNoAgency(arrivalsAndDeparture.getStop().getId()));
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
						for(Long weekendTime : DateUtil.getWeekendDateTimes(agency.getTimezone())){
							ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
							query.setTime(weekendTime);
							query.setMinutesBefore(0);
							query.setMinutesAfter(MINUTES_IN_DAY);
							
							StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
							
							for(ArrivalAndDepartureBean arrivalsAndDeparture : stopsWithArrivals.getArrivalsAndDepartures()){
								
								// Filter Arrivals and Departures By Route
								if(arrivalsAndDeparture.getTrip().getRoute().getId().equals(route)){
	
									ScheduleStop scheduleStop = new ScheduleStop();
									scheduleStop.setTag(getIdNoAgency(arrivalsAndDeparture.getStop().getId()));
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
					scheduleRoute.setTitle(stopsForRoute.getRoute().getLongName());
					scheduleRoute.setDirection(Integer.toString(n%2));
					scheduleRoute.setTag(getIdNoAgency(stopsForRoute.getRoute().getId()));
					scheduleRoute.setServiceClass(n < 3 ? "wkd" : "wkend");
			
					// Blocks
					for (Entry<String, HashSet<ScheduleStop>> entry : blockStopsMap.entrySet()) {
						int tripStopTimeCounter = 0;
					    String blockId = entry.getKey();
					    HashSet<ScheduleStop> blockStops = entry.getValue();
					    
					    ScheduleTableRow scheduleTr = new ScheduleTableRow(getIdNoAgency(blockId));
					    
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
	
	private boolean isValid(Body body, List<AgencyAndId> routeIds) {
		if (!isValidAgency(body, agencyId))
			return false;
		
		List<String> agencies = new ArrayList<String>();
		agencies.add(agencyId);
		
		if(!processRouteIds(routeId, routeIds, agencies, body))
			return false;
		
		return true;
	}
}
