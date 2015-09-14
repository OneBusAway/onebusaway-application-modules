package org.onebusaway.nextbus.actions.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.onebusaway.nextbus.impl.ConversionUtil;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class Schedule {

	@Autowired
	protected TransitDataService _service;
	
	
	public TransitDataService get_service() {
		return _service;
	}

	public void set_service(TransitDataService _service) {
		this._service = _service;
	}
	
	protected static final int MINUTES_IN_DAY = 60 * 24;
	
	protected SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
	
	
	protected TripDetailsBean getTripDetails(String tripId, Date date){
		TripDetailsQueryBean tripDetailsQuery = new TripDetailsQueryBean();
		tripDetailsQuery.setTripId(tripId);
		tripDetailsQuery.setServiceDate(ConversionUtil.getStartofDayTime(date));
		tripDetailsQuery.setTime(date.getTime());
		return _service.getSingleTripDetails(tripDetailsQuery);
	}
	
	protected List<TripDetailsBean> getTripDetailsForRoute(String routeId, Date date){
		TripsForRouteQueryBean tripRouteQuery = new TripsForRouteQueryBean();
		tripRouteQuery.setTime(date.getTime());
		tripRouteQuery.setRouteId(routeId);		
		
		ListBean<TripDetailsBean> tripDetails = _service.getTripsForRoute(tripRouteQuery);
		return tripDetails.getList();
	}
	
	/*protected List<TripDetailsBean> getTripDetailsForRouteDirection0(String routeId, Date date, String direction){
		StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);	
		for(StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()){
			for(StopGroupBean stopGroupBean: stopGroupingBean.getStopGroups()){
				if(stopGroupBean.getId().equals(direction)){
					ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
					query.setTime(ConversionUtil.getStartofDayTime(date));
					query.setMinutesBefore(0);
					query.setMinutesAfter(MINUTES_IN_DAY);
					
					StopsWithArrivalsAndDeparturesBean stopsWithArrivals = _service.getStopsWithArrivalsAndDepartures(stopGroupBean.getStopIds(), query);
				}
			}
		}
		
		ListBean<TripDetailsBean> tripDetails = _service.getTripsForRoute(tripRouteQuery);
		return tripDetails.getList();
	}*/
	
	protected String getStartTime(TripDetailsBean tripDetails){
		if(tripDetails != null && tripDetails.getSchedule().getStopTimes().size() >  0)
			return sdf.format(new Date(tripDetails.getServiceDate() + 
					(tripDetails.getSchedule().getStopTimes().get(0).getDepartureTime() * 1000)));
		return null;
	}
	
	protected String getEndTime(TripDetailsBean tripDetails){
		if(tripDetails != null && tripDetails.getSchedule().getStopTimes().size() > 1 ){
			return sdf.format(new Date(tripDetails.getServiceDate() + 
					(tripDetails.getSchedule().getStopTimes().get(
							tripDetails.getSchedule().getStopTimes().size() - 1).getDepartureTime() * 1000)));			
		}
		return getStartTime(tripDetails);
	}
}
