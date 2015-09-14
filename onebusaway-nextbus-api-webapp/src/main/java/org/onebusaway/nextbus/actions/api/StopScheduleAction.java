package org.onebusaway.nextbus.actions.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.nextbus.impl.ConversionUtil;
import org.onebusaway.nextbus.model.RouteScheduleInfo;
import org.onebusaway.nextbus.model.StopScheduleArrival;
import org.onebusaway.nextbus.model.StopScheduleInfo;
import org.onebusaway.nextbus.model.StopTime;
import org.onebusaway.nextbus.model.Trip;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopTimeGroupBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class StopScheduleAction extends Schedule implements ModelDriven<StopScheduleInfo> {
	
	private String stopId;
	
	private Date date;
	
	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public StopScheduleInfo getModel() {
		StopScheduleInfo ssi = new StopScheduleInfo();
		
		ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
		query.setTime(ConversionUtil.getStartofDayTime(getDate()));
		query.setMinutesBefore(0);
		query.setMinutesAfter(MINUTES_IN_DAY);
		
		try{
			StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = _service.getStopWithArrivalsAndDepartures(stopId, query);
		
			for(ArrivalAndDepartureBean arrivalAndDepartureBean : stopWithArrivalsAndDepartures.getArrivalsAndDepartures()){
				StopScheduleArrival stopScheduleArrival = new StopScheduleArrival();
				
				stopScheduleArrival.setTripId(arrivalAndDepartureBean.getTrip().getId());
				stopScheduleArrival.setTripHeadsign(arrivalAndDepartureBean.getTrip().getTripHeadsign());
				stopScheduleArrival.setDirectionNum(arrivalAndDepartureBean.getTrip().getDirectionId());
				stopScheduleArrival.setTime(sdf.format(new Date(arrivalAndDepartureBean.getScheduledArrivalTime())));
				stopScheduleArrival.setRouteId(arrivalAndDepartureBean.getTrip().getRoute().getId());
				//stopScheduleArrival.setTripDirectionText();
				
				TripDetailsBean tripDetails = getTripDetails(arrivalAndDepartureBean.getTrip().getId(), getDate());
				
				stopScheduleArrival.setStartTime(getStartTime(tripDetails));
				stopScheduleArrival.setStartTime(getEndTime(tripDetails));
				
				ssi.getScheduleArrivals().add(stopScheduleArrival);
			}
		}
		catch(NoSuchStopServiceException nsse){
			//TODO - Handle Exception
		}
	

		return ssi;
		
	}

	public String getStopId() {
		return stopId;
	}
	
	@RequiredFieldValidator
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public Date getDate() {
		if(date == null)
			return new Date();
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
