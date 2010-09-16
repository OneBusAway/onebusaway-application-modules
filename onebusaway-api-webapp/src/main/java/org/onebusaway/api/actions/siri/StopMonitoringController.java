package org.onebusaway.api.actions.siri;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.siri.model.FramedVehicleJourneyRef;
import org.onebusaway.siri.model.MonitoredStopVisit;
import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.StopMonitoringDelivery;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
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
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * For a given stop, return the vehicles.
 */
public class StopMonitoringController implements ModelDriven<Object>,
    ServletRequestAware {

  private Object _response;
  private HttpServletRequest _request;

  @Autowired
  private TransitDataService _transitDataService;

  /**
   * This is the default action for
   * 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {

    /* find the stop */
    String stopId = _request.getParameter("stopId");
    if (stopId == null) {
      throw new IllegalArgumentException("Expected parameter stopId");
    }
    String agencyId = _request.getParameter("agencyId");
    if (agencyId == null) {
      throw new IllegalArgumentException("Expected parameter agencyId");
    }

    AgencyBean agency = _transitDataService.getAgency(agencyId);
    if (agency == null) {
      throw new IllegalArgumentException("No such agency: " + agencyId);
    }

    String routeId = _request.getParameter("routeId");
    String directionId = _request.getParameter("directionId");

    // convert ids to agency_and_id
    stopId = agencyId + "_" + stopId;

    if (routeId != null) {
      routeId = agencyId + "_" + routeId;
    }
    if (directionId != null) {
      directionId = agencyId + "_" + directionId;
    }

    Calendar timeFrom = new GregorianCalendar();
    timeFrom.add(Calendar.MINUTE, -30);
    Calendar timeTo = new GregorianCalendar();
    timeTo.add(Calendar.MINUTE, 30);

    StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = _transitDataService.getStopWithArrivalsAndDepartures(
        stopId, timeFrom.getTime(), timeTo.getTime());

    GregorianCalendar now = new GregorianCalendar();
    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    siri.ServiceDelivery.ResponseTimestamp = now;

    siri.ServiceDelivery.ProducerRef = _request.getServerName();
    siri.ServiceDelivery.deliveries = new ArrayList<StopMonitoringDelivery>();

    StopMonitoringDelivery delivery = new StopMonitoringDelivery();
    siri.ServiceDelivery.deliveries.add(delivery);

    delivery.ResponseTimestamp = now;
    delivery.visits = new ArrayList<MonitoredStopVisit>();

    for (ArrivalAndDepartureBean adbean : stopWithArrivalsAndDepartures.getArrivalsAndDepartures()) {
      TripBean trip = adbean.getTrip();
      RouteBean route = trip.getRoute();
      if (routeId != null && !route.getId().equals(routeId)) {
        // filtered out
        continue;
      }
      if (directionId != null && !trip.getDirectionId().equals(directionId)) {
        // filtered out
        continue;
      }

      /* gather data about trip, route, and stops */
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTripId(trip.getId());
      query.setServiceDate(adbean.getServiceDate());
      query.setTime(now.getTime().getTime());
      TripDetailsBean specificTripDetails = _transitDataService.getSpecificTripDetails(query);

      StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(route.getId());
      List<StopBean> stops = stopsForRoute.getStops();

      MonitoredStopVisit MonitoredStopVisit = new MonitoredStopVisit();
      delivery.visits.add(MonitoredStopVisit);

      MonitoredStopVisit.RecordedAtTime = new GregorianCalendar();
      TripStatusBean status = specificTripDetails.getStatus();

      MonitoredStopVisit.RecordedAtTime.setTimeInMillis(status.getLastUpdateTime());
      MonitoredStopVisit.MonitoringRef = adbean.getStopId();
      MonitoredStopVisit.MonitoredVehicleJourney = new MonitoredVehicleJourney();
      MonitoredStopVisit.MonitoredVehicleJourney.LineRef = route.getId();
      MonitoredStopVisit.MonitoredVehicleJourney.DirectionRef = trip.getDirectionId();
      MonitoredStopVisit.MonitoredVehicleJourney.VehicleRef = status.getVehicleId();
      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef = new FramedVehicleJourneyRef();

      CoordinatePoint position = status.getLocation();
      if (position != null) {
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation = new VehicleLocation();
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Latitude = status.getLocation().getLat();
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Longitude = status.getLocation().getLon();

        MonitoredStopVisit.MonitoredVehicleJourney.DistanceAlongRoute = status.getDistanceAlongTrip();
        MonitoredStopVisit.MonitoredVehicleJourney.DistanceFromCall = adbean.getDistanceFromStop();

        int i = 0;
        boolean started = false;
        for (TripStopTimeBean stopTime : specificTripDetails.getSchedule().getStopTimes()) {
          if (started) {
            i++;
          }
          if (stopTime.getStop().equals(
              specificTripDetails.getStatus().getClosestStop())) {
            started = true;
          }
          if (stopTime.getStop().getId().equals(stopId)) {
            break;
          }
        }

        MonitoredStopVisit.MonitoredVehicleJourney.StopsFromCall = i;
      }
      Date serviceDate = new Date(adbean.getServiceDate());

      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef.DataFrameRef = String.format(
          "%1$tY-%1$tm-%1$td", serviceDate);
      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef.DatedVehicleJourneyRef = trip.getId();
      MonitoredStopVisit.MonitoredVehicleJourney.PublishedLineName = trip.getTripHeadsign();

      MonitoredStopVisit.MonitoredVehicleJourney.OriginRef = stops.get(0).getId();
      MonitoredStopVisit.MonitoredVehicleJourney.DestinationRef = stops.get(
          stops.size() - 1).getId();
    }

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
