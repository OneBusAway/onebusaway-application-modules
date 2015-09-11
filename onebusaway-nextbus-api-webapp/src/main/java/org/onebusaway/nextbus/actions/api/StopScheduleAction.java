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

public class StopScheduleAction implements ModelDriven<StopScheduleInfo> {
	
	@Autowired
	private TransitDataService _service;
	
	private String stopId;
	
	private Date date;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
	
	private static final int MINUTES_IN_DAY = 60 * 24;
	
	
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
	public StopScheduleInfo getModel() {
		StopScheduleInfo ssi = new StopScheduleInfo();
		
		ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
		query.setTime(ConversionUtil.getStartofDayTime(getDate()));
		query.setMinutesBefore(0);
		query.setMinutesAfter(MINUTES_IN_DAY);
		
		StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = _service.getStopWithArrivalsAndDepartures(stopId, query);
		
		for(ArrivalAndDepartureBean arrivalAndDepartureBean : stopWithArrivalsAndDepartures.getArrivalsAndDepartures()){
			StopScheduleArrival stopScheduleArrival = new StopScheduleArrival();
			
			stopScheduleArrival.setTripId(arrivalAndDepartureBean.getTrip().getId());
			stopScheduleArrival.setTripHeadsign(arrivalAndDepartureBean.getTripHeadsign());
			stopScheduleArrival.setDirectionNum(arrivalAndDepartureBean.getTrip().getDirectionId());
			stopScheduleArrival.setTime(sdf.format(new Date(arrivalAndDepartureBean.getScheduledArrivalTime())));
			stopScheduleArrival.setRouteId(arrivalAndDepartureBean.getTrip().getRoute().getId());
			
			List<TripDetailsBean> tripDetailsList = getTripDetails(arrivalAndDepartureBean.getTrip().getRoute().getId(), getDate().getTime());
			
			for(TripDetailsBean tripDetails : tripDetailsList){
				
				if(tripDetails.getTripId() == arrivalAndDepartureBean.getTrip().getId() && tripDetails.getSchedule().getStopTimes().size() >  0){
					stopScheduleArrival.setStartTime(sdf.format(new Date(tripDetails.getServiceDate() + 
							(tripDetails.getSchedule().getStopTimes().get(0).getDepartureTime() * 1000))));
					if(tripDetails.getSchedule().getStopTimes().size() > 1 ){
						stopScheduleArrival.setEndTime(sdf.format(new Date(tripDetails.getServiceDate() + 
								(tripDetails.getSchedule().getStopTimes().get(
										tripDetails.getSchedule().getStopTimes().size() - 1).getDepartureTime() * 1000)
								)
						));
					}
					else{
						stopScheduleArrival.setEndTime(stopScheduleArrival.getStartTime());
					}
				}
				
			}
			
			//stopScheduleArrival.setTripDirectionText();
			
			ssi.getScheduleArrivals().add(stopScheduleArrival);
		}
		
		
		/*StopScheduleBean stopSchedule = _service.getScheduleForStop(stopId, getDate());
		
		if(stopSchedule != null){
			for(StopRouteScheduleBean stopRouteSchedule : stopSchedule.getRoutes()){
				
				for(int dir=0; dir<stopRouteSchedule.getDirections().size(); dir++){
					for(StopTimeInstanceBean stopTimeInstance : stopRouteSchedule.getDirections().get(dir).getStopTimes()){
							
							StopScheduleArrival stopScheduleArrival = new StopScheduleArrival();
							
							stopScheduleArrival.setDirectionNum(dir);
							stopScheduleArrival.setTripId(stopTimeInstance.getTripId());
							stopScheduleArrival.setTripHeadsign(stopRouteSchedule.getDirections().get(dir).getTripHeadsign());
							stopScheduleArrival.setTime(sdf.format(new Date(stopTimeInstance.getArrivalTime())));
							stopScheduleArrival.setRouteId(stopRouteSchedule.getRoute().getId());
							
							//TripBean trip = _service.getTrip(stopTimeInstance.getTripId());
							
							TripDetailsQueryBean query = new TripDetailsQueryBean();
							query.setTripId(stopTimeInstance.getTripId());
							query.setServiceDate(getDate().getTime());
							query.setTime(getDate().getTime());
							
							TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
							inclusionBean.setIncludeTripBean(true);
							inclusionBean.setIncludeTripStatus(true);
							
							query.setInclusion(inclusionBean);
							
							
							ListBean<TripDetailsBean> tripDetails = _service.getTripDetails(query);
							
							
							//stopScheduleArrival.setTripDirectionText();
							//stopScheduleArrival.setStartTime(stopTimeInstance.get);
							//stopScheduleArrival.setEndTime(startTime);
							
							ssi.getScheduleArrivals().add(stopScheduleArrival);
					}
				}
			}
		}*/

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
	
	private List<TripDetailsBean> getTripDetails(String routeId, long time){
		TripsForRouteQueryBean tripRouteQuery = new TripsForRouteQueryBean();
		tripRouteQuery.setTime(time);
		tripRouteQuery.setRouteId(routeId);
		
		TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
		inclusionBean.setIncludeTripBean(true);
		inclusionBean.setIncludeTripStatus(true);
		
		tripRouteQuery.setInclusion(inclusionBean);
		
		
		ListBean<TripDetailsBean> tripDetails = _service.getTripsForRoute(tripRouteQuery);
		return tripDetails.getList();
	}

}
