package org.onebusaway.api.actions.siri;

import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ProgressBetweenStops;
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
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

/**
 * For a given route, returns the vehicle ID, route, destination, last location
 * and time of last observation of each bus serving the route, as well as
 * remaining stops for each of the buses. distance / number of stops away data,
 * if available
 */
public class VehicleMonitoringController implements ModelDriven<ServiceDelivery>, ServletRequestAware {

  private ServiceDelivery response;
  private HttpServletRequest request;
  
  @Autowired  
  private TransitDataService service;
  
  /**
   * This is the default action for 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {
    
    /* find the vehicle */
    String vehicleId = request.getParameter("vehicleId");
    String agencyId = request.getParameter("agencyId");
    
    AgencyBean agency = service.getAgency(agencyId);
    TimeZone timeZone = TimeZone.getTimeZone(agency.getTimezone());
    Calendar now = Calendar.getInstance(timeZone);
    
    TripForVehicleQueryBean query = new TripForVehicleQueryBean();
    query.setVehicleId(agencyId + "_" + vehicleId);
    query.setTime(now.getTime());
    
    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(true);
    inclusion.setIncludeTripSchedule(true);
    inclusion.setIncludeTripStatus(true);

    TripDetailsBean trip = service.getTripDetailsForVehicleAndTime(query);
    
    if (trip == null)
      return null;
    
    response = new ServiceDelivery();
    response.ResponseTimestamp = now;
    
    response.ProducerRef = request.getServerName();
    response.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    response.VehicleMonitoringDelivery.ResponseTimestamp = response.ResponseTimestamp;
    response.VehicleMonitoringDelivery.SubscriberRef = request.getRemoteAddr();

    VehicleActivity activity = new VehicleActivity();

    TripStatusBean status = trip.getStatus();
    double totalDistance = status.getDistanceAlongRoute();
    
    
    activity.RecordedAtTime = now; //fixme: get this from somewhere
    activity.MonitoredVehicleJourney = new MonitoredVehicleJourney();
    activity.MonitoredVehicleJourney.Bearing = 0; //fixme: get this
    activity.MonitoredVehicleJourney.BlockRef = trip.getTrip().getBlockId();
    activity.MonitoredVehicleJourney.CourseOfJourneyRef = trip.getTripId();
    VehicleLocation location = new VehicleLocation();
    location.Latitude = 0; //fixme: get this
    location.Longitude = 0; //fixme: get this
    location.Precision = 0; //fixme: get this
    
    activity.MonitoredVehicleJourney.VehicleLocation = location;
    response.VehicleMonitoringDelivery.deliveries = new ArrayList<VehicleActivity>();
    response.VehicleMonitoringDelivery.deliveries.add(activity);
    
    return new DefaultHttpHeaders();
  }

  
  @Override
  public ServiceDelivery getModel() {
    return response;
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }


  public void setService(TransitDataService service) {
    this.service = service;
  }


  public TransitDataService getService() {
    return service;
  }

}
