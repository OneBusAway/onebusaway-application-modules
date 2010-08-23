package org.onebusaway.api.actions.siri;

import org.onebusaway.siri.model.ErrorMessage;
import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
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
 * For a given vehicle, returns the location
 */
public class VehicleMonitoringController implements ModelDriven<Object>,
    ServletRequestAware {

  private Object _response;
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
    if (vehicleId == null) {
      throw new IllegalArgumentException("Expected parameter vehicleId");
    }
    String agencyId = _request.getParameter("agencyId");
    if (agencyId == null) {
      throw new IllegalArgumentException("Expected parameter agencyIdag");
    }
    
    AgencyBean agency = _transitDataService.getAgency(agencyId);
    if (agency == null) {
      throw new IllegalArgumentException("No such agency: " + agencyId);
    }
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
    
    if (trip == null) {
      /*
       * SIRI doesn't really specify what to do here, as far as I can tell. So
       * we'll return our own type of error,
       */
      _response = new ErrorMessage("No known trip for this vehicle");
      return new DefaultHttpHeaders();
    }

    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    siri.ServiceDelivery.ResponseTimestamp = now;
    
    siri.ServiceDelivery.ProducerRef = _request.getServerName();
    siri.ServiceDelivery.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ResponseTimestamp = siri.ServiceDelivery.ResponseTimestamp;
    siri.ServiceDelivery.VehicleMonitoringDelivery.SubscriberRef = _request.getRemoteAddr();

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
    siri.ServiceDelivery.VehicleMonitoringDelivery.deliveries = new ArrayList<VehicleActivity>();
    siri.ServiceDelivery.VehicleMonitoringDelivery.deliveries.add(activity);
    
    _response = siri;
    return new DefaultHttpHeaders();
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
