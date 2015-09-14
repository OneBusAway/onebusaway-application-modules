package org.onebusaway.nextbus.actions.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.impl.ConversionUtil;
import org.onebusaway.nextbus.model.RouteScheduleInfo;
import org.onebusaway.nextbus.model.StopTime;
import org.onebusaway.nextbus.model.Trip;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.comparators.AlphanumComparator;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RouteScheduleAction extends Schedule implements ModelDriven<RouteScheduleInfo> {
	
	@Autowired
	private TransitDataService _service;
	
	private String routeId;
	
	private Date date;
	
	private boolean includingVariations;
	
	
	public TransitDataService get_service() {
		return _service;
	}

	public void set_service(TransitDataService _service) {
		this._service = _service;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders();
	}

	@Override
	public RouteScheduleInfo getModel() {
		RouteScheduleInfo rsi = new RouteScheduleInfo();
		
		// Keep Track of Trips
		Map<String,Trip> tripsMap = new HashMap<String, Trip>();
		
		//Get All the Stops for a specific route
		StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);
		for(StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()){
			for(StopGroupBean stopGroupBean: stopGroupingBean.getStopGroups()){

				ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
				query.setTime(ConversionUtil.getStartofDayTime(date));
				query.setMinutesBefore(0);
				query.setMinutesAfter(MINUTES_IN_DAY);
				
				StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
				
				for(ArrivalAndDepartureBean arrivalsAndDeparture : stopsWithArrivals.getArrivalsAndDepartures()){
					// Filter Arrivals and Departures By Route
					if(arrivalsAndDeparture.getTrip().getRoute().getId().equals(routeId)){
						
						TripBean trip = arrivalsAndDeparture.getTrip();
						String tripId = trip.getId();
			
						if(!tripsMap.containsKey(tripId)){
							Trip nextBusTrip = new Trip();
							nextBusTrip.setTripId(trip.getId());
							nextBusTrip.setRouteId(trip.getRoute().getId());
							nextBusTrip.setTripHeadsign(trip.getTripHeadsign());
							nextBusTrip.setTripDirectionText(stopGroupBean.getName().getName());
							nextBusTrip.setDirectionNum(trip.getDirectionId());
								
							tripsMap.put(tripId, nextBusTrip);
						}
						
						StopTime nextBusStopTime = new StopTime();
						nextBusStopTime.setStopId(arrivalsAndDeparture.getStop().getId());
						nextBusStopTime.setStopName(arrivalsAndDeparture.getStop().getName());		
						nextBusStopTime.setTime(sdf.format(new Date(arrivalsAndDeparture.getScheduledArrivalTime())));
						nextBusStopTime.setStopSeq(arrivalsAndDeparture.getStopSequence());

						tripsMap.get(tripId).getStopTimes().add(nextBusStopTime);

					}
				}	
			}
		}
		
		for (Trip trip : tripsMap.values()) {
			if(trip.getDirectionNum()!= null){
			    if(trip.getDirectionNum().equals("0")){
			    	Collections.sort(trip.getStopTimes(), new StopTimeComparator());
			    	if(trip.getStopTimes().size() > 0){
			    		trip.setStartTime(trip.getStopTimes().get(0).getTime());
			    		trip.setEndTime(trip.getStopTimes().get(trip.getStopTimes().size() - 1).getTime());
			    	}
			    	rsi.getDirection0().add(trip);
			    }
			    else if(trip.getDirectionNum().equals("1")){
			    	rsi.getDirection1().add(trip);
			    }
			}
		   
		}
		
		Collections.sort(rsi.getDirection0(),new TripComparator());
		Collections.sort(rsi.getDirection1(),new TripComparator());
		
		return rsi;
		
	}

	public String getRouteId() {
		return routeId;
	}
	
	@RequiredFieldValidator
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public Date getDate() {
		if(date == null)
			return new Date();
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isIncludingVariations() {
		return includingVariations;
	}

	public void setIncludingVariations(boolean includingVariations) {
		this.includingVariations = includingVariations;
	}
	
	private class StopTimeComparator implements Comparator<StopTime> {
		
		private Comparator<String> alphaNumComparator = new AlphanumComparator();
		
	    @Override
	    public int compare(StopTime s1, StopTime s2) {
	    	return alphaNumComparator.compare(s1.getTime(), s2.getTime());
	    }
	}
	
	private class TripComparator implements Comparator<Trip> {
		
		private Comparator<String> alphaNumComparator = new AlphanumComparator();
		
	    @Override
	    public int compare(Trip t1, Trip t2) {
	    	return alphaNumComparator.compare(t1.getStartTime(), t2.getStartTime());
	    }
	}
	

}
