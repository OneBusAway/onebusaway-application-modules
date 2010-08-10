package org.onebusaway.api.actions.siri;

import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.VehicleActivity;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.siri.model.VehicleMonitoringDelivery;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
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
 * For a given route, returns the vehicle ID, route, destination, last location
 * and time of last observation of each bus serving the route, as well as
 * remaining stops for each of the buses. distance / number of stops away data,
 * if available
 */
public class VehicleMonitoringController implements ModelDriven<ServiceDelivery>, ServletRequestAware {

  private ServiceDelivery _response;
  private HttpServletRequest _request;
  
  @Autowired  
  private TransitDataService _transitDataService;
  
  /**
   * This is the default action for 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {
    
    /* find the vehicle */
    String vehicleId = _request.getParameter("vehicleId");
    String agencyId = _request.getParameter("agencyId");
    
    AgencyBean agency = _transitDataService.getAgency(agencyId);
    TimeZone timeZone = TimeZone.getTimeZone(agency.getTimezone());
    Calendar now = Calendar.getInstance(timeZone);
    
    TripForVehicleQueryBean query = new TripForVehicleQueryBean();
    query.setVehicleId(agencyId + "_" + vehicleId);
    query.setTime(now.getTime());
    
    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(true);
    inclusion.setIncludeTripSchedule(true);
    inclusion.setIncludeTripStatus(true);

    TripDetailsBean trip = _transitDataService.getTripDetailsForVehicleAndTime(query);
    
    if (trip == null)
      return null;
    
    _response = new ServiceDelivery();
    _response.ResponseTimestamp = now;
    
    _response.ProducerRef = _request.getServerName();
    _response.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    _response.VehicleMonitoringDelivery.ResponseTimestamp = _response.ResponseTimestamp;
    _response.VehicleMonitoringDelivery.SubscriberRef = _request.getRemoteAddr();

    VehicleActivity activity = new VehicleActivity();

    TripStatusBean status = trip.getStatus();
    
    Calendar time = Calendar.getInstance();
    time.setTime(new Date(status.getTime()));
    
    activity.RecordedAtTime = time;
    activity.MonitoredVehicleJourney = new MonitoredVehicleJourney();
    activity.MonitoredVehicleJourney.BlockRef = trip.getTrip().getBlockId();
    activity.MonitoredVehicleJourney.CourseOfJourneyRef = trip.getTripId();
    VehicleLocation location = new VehicleLocation();
    location.Latitude = status.getPosition().getLat();
    location.Longitude = status.getPosition().getLon();
    
    activity.MonitoredVehicleJourney.VehicleLocation = location;
    _response.VehicleMonitoringDelivery.deliveries = new ArrayList<VehicleActivity>();
    _response.VehicleMonitoringDelivery.deliveries.add(activity);
    
    return new DefaultHttpHeaders();
  }

  
  @Override
  public ServiceDelivery getModel() {
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
