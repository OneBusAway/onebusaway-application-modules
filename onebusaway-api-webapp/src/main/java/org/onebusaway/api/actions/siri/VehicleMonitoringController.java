package org.onebusaway.api.actions.siri;

import org.onebusaway.siri.model.ErrorMessage;
import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.VehicleActivity;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.siri.model.VehicleMonitoringDelivery;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.ModelDriven;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

/**
 * For a given vehicle, returns the location
 */
public class VehicleMonitoringController implements ModelDriven<Object>,
    ServletRequestAware {

  private Object _response;
  private HttpServletRequest _request;
  
  @Autowired  
  private TransitDataService _transitDataService;
  private TimeZone defaultTimeZone = TimeZone.getTimeZone("America/New York");
  
  /**
   * This is the default action for 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {
    
    String agencyId = _request.getParameter("agencyId");
    TimeZone timeZone;
    if (agencyId == null) {
      timeZone = defaultTimeZone;
    } else {
      AgencyBean agency = _transitDataService.getAgency(agencyId);
      if (agency == null) {
        throw new IllegalArgumentException("No such agency: " + agencyId);
      }
      timeZone = TimeZone.getTimeZone(agency.getTimezone());
    }

    String vehicleId = _request.getParameter("vehicleId");

    if (vehicleId != null) {
      return singleVehicleTrip(agencyId, vehicleId, timeZone);
    }

    // single trip, by trip (but wait there might be more than one!)
    String tripId = _request.getParameter("tripId");
    if (tripId != null) {
      TripBean trip = _transitDataService.getTrip(agencyId + "_" + tripId);
      if (trip == null) {
        throw new IllegalArgumentException("No such trip: " + tripId);
      }
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTripId(tripId);
      TripDetailsBean tripDetails = _transitDataService.getSpecificTripDetails(query);
      return singleVehicleTrip(agencyId, tripDetails, timeZone);
    }

    String routeId = _request.getParameter("routeId");
    // multiple trips by route
    if (routeId != null) {
      TripsForRouteQueryBean query = new TripsForRouteQueryBean();
      query.setRouteId(agencyId + "_" + routeId);
      Calendar now = Calendar.getInstance(timeZone);
      query.setTime(now.getTimeInMillis());
      ListBean<TripDetailsBean> trips = _transitDataService.getTripsForRoute(query);
      ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
      for (TripDetailsBean trip : trips.getList()) {
        VehicleActivity activity = createActivity(trip);
        activities.add(activity);
      }
      _response = generateSiriResponse(now, activities);

      return new DefaultHttpHeaders();
    }

    return new DefaultHttpHeaders();
  }

  private DefaultHttpHeaders singleVehicleTrip(String agencyId,
      TripDetailsBean trip, TimeZone timeZone) {

    Calendar now = Calendar.getInstance(timeZone);

    VehicleActivity activity = createActivity(trip);

    ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
    activities.add(activity);
    Siri siri = generateSiriResponse(now, activities);

    _response = siri;
    return new DefaultHttpHeaders();

  }

  private Siri generateSiriResponse(Calendar now,
      ArrayList<VehicleActivity> activities) {
    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    siri.ServiceDelivery.ResponseTimestamp = now;
    
    siri.ServiceDelivery.ProducerRef = _request.getServerName();
    siri.ServiceDelivery.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ResponseTimestamp = siri.ServiceDelivery.ResponseTimestamp;
    siri.ServiceDelivery.VehicleMonitoringDelivery.SubscriberRef = _request.getRemoteAddr();
    
    siri.ServiceDelivery.VehicleMonitoringDelivery.deliveries = activities;
    return siri;
  }

  private VehicleActivity createActivity(TripDetailsBean trip) {
    VehicleActivity activity = new VehicleActivity();
    TripStatusBean status = trip.getStatus();
    
    Calendar time = Calendar.getInstance();
    time.setTime(new Date(status.getLastUpdateTime()));
    
    activity.RecordedAtTime = time;
    activity.MonitoredVehicleJourney = new MonitoredVehicleJourney();
    activity.MonitoredVehicleJourney.BlockRef = trip.getTrip().getBlockId();
    activity.MonitoredVehicleJourney.CourseOfJourneyRef = trip.getTripId();
    VehicleLocation location = new VehicleLocation();
    location.Latitude = status.getLocation().getLat();
    location.Longitude = status.getLocation().getLon();
    
    activity.MonitoredVehicleJourney.VehicleLocation = location;
    return activity;
  }

  private DefaultHttpHeaders singleVehicleTrip(String agencyId,
      String vehicleId, TimeZone timeZone) {

    Calendar now = Calendar.getInstance(timeZone);

    /* find the vehicle's trip */
    TripForVehicleQueryBean query = new TripForVehicleQueryBean();
    query.setVehicleId(agencyId + "_" + vehicleId);
    query.setTime(now.getTime());

    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(true);
    inclusion.setIncludeTripSchedule(true);
    inclusion.setIncludeTripStatus(true);

    TripDetailsBean trip = _transitDataService.getTripDetailsForVehicleAndTime(query);

    if (trip == null) {
      /*
       * This vehicle isn't on a trip, so
       */
      _response = new ErrorMessage("No known trip for this vehicle");
      return new DefaultHttpHeaders();
    }

    return singleVehicleTrip(agencyId, trip, timeZone);
  }


  @Override
  public Object getModel() {
    return _response;
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this._request = request;
  }


  public void setService(TransitDataService service) {
    this._transitDataService = service;
  }


  public TransitDataService getService() {
    return _transitDataService;
  }

}
