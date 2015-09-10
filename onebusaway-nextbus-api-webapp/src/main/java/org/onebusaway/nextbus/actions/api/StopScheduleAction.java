package org.onebusaway.nextbus.actions.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.impl.ConversionUtil;
import org.onebusaway.nextbus.model.RouteScheduleInfo;
import org.onebusaway.nextbus.model.StopTime;
import org.onebusaway.nextbus.model.Trip;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class StopScheduleAction implements ModelDriven<RouteScheduleInfo> {
	
	@Autowired
	private TransitDataService _service;
	
	private String routeId;
	
	private Date date;
	
	private boolean includingVariations;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
	
	
	public TransitDataService get_service() {
		return _service;
	}

	public void set_service(TransitDataService _service) {
		this._service = _service;
	}
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public RouteScheduleInfo getModel() {
		RouteScheduleInfo rsi = new RouteScheduleInfo();
		
		for(TripDetailsBean tripDetails : getTripDetails()){
			Trip nextBusTrip = new Trip();
			
			
			if(tripDetails.getTrip() != null){
				nextBusTrip.setTripID(tripDetails.getTrip().getId());
				nextBusTrip.setRouteId(tripDetails.getTrip().getRoute().getId());
				nextBusTrip.setTripHeadsign(tripDetails.getTrip().getTripHeadsign());
				nextBusTrip.setTripDirectionText(tripDetails.getTrip().getDirectionId());
				
				int stopSequence = 0;
				
				for(TripStopTimeBean tripStopTime : tripDetails.getSchedule().getStopTimes()){
					StopTime nextBusStopTime = new StopTime();
					nextBusStopTime.setStopId(tripStopTime.getStop().getId());
					nextBusStopTime.setStopName(tripStopTime.getStop().getName());		
					nextBusStopTime.setTime(sdf.format(new Date(tripDetails.getServiceDate() + (tripStopTime.getDepartureTime() * 1000))));
					nextBusStopTime.setStopSeq(stopSequence);
					stopSequence++;
					
					nextBusTrip.getStopTimes().add(nextBusStopTime);				
				}
				
				if(tripDetails.getTrip().getDirectionId().equals("0"))
					rsi.getDirection0().add(nextBusTrip);
				else if (tripDetails.getTrip().getDirectionId().equals("1"))
					rsi.getDirection1().add(nextBusTrip);
			}
		}
		
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
		this.date = ConversionUtil.convertLocalDateToDateTimezone(date, "EST");
	}

	public boolean isIncludingVariations() {
		return includingVariations;
	}

	public void setIncludingVariations(boolean includingVariations) {
		this.includingVariations = includingVariations;
	}
	
	private List<TripDetailsBean> getTripDetails(){
		TripsForRouteQueryBean tripRouteQuery = new TripsForRouteQueryBean();
		tripRouteQuery.setTime(getDate().getTime());
		tripRouteQuery.setRouteId(routeId);
		
		TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
		inclusionBean.setIncludeTripBean(true);
		inclusionBean.setIncludeTripStatus(true);
		
		tripRouteQuery.setInclusion(inclusionBean);
		
		
		ListBean<TripDetailsBean> tripDetails = _service.getTripsForRoute(tripRouteQuery);
		return tripDetails.getList();
	}

}
