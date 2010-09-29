package org.onebusaway.api.actions.siri;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.siri.model.DistanceExtensions;
import org.onebusaway.siri.model.FramedVehicleJourneyRef;
import org.onebusaway.siri.model.MonitoredCall;
import org.onebusaway.siri.model.MonitoredStopVisit;
import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.OnwardCall;
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
import java.util.HashMap;
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
    String stopId = _request.getParameter("MonitoringRef");
    if (stopId == null) {
      throw new IllegalArgumentException("Expected parameter MonitoringRef");
    }
    String agencyId = _request.getParameter("OperatorRef");
    if (agencyId == null) {
      throw new IllegalArgumentException("Expected parameter OperatorRef");
    }

    AgencyBean agency = _transitDataService.getAgency(agencyId);
    if (agency == null) {
      throw new IllegalArgumentException("No such agency: " + agencyId);
    }

    String routeId = _request.getParameter("LineRef");
    String directionId = _request.getParameter("DirectionRef");

    String detailLevel = _request.getParameter("StopMonitoringDetailLevel");
    boolean includeOnwardCalls = false;
    if (detailLevel != null) {
      includeOnwardCalls = detailLevel.equals("calls");
    }

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

    if (stopWithArrivalsAndDepartures.getStop() == null) {
      throw new IllegalArgumentException("Bogus stop parameter");
    }

    GregorianCalendar now = new GregorianCalendar();
    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    siri.ServiceDelivery.ResponseTimestamp = now;
    siri.ServiceDelivery.stopMonitoringDeliveries = new ArrayList<StopMonitoringDelivery>();

    StopMonitoringDelivery delivery = new StopMonitoringDelivery();
    siri.ServiceDelivery.stopMonitoringDeliveries.add(delivery);

    delivery.ResponseTimestamp = now;

    delivery.ValidUntil = (Calendar) now.clone();
    delivery.ValidUntil.add(Calendar.MINUTE, 1);

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

      TripStatusBean status = specificTripDetails.getStatus();
      if (status == null) {
        // this trip has no status. Let's skip it.
        continue;
      }

      StopBean closestStop = status.getClosestStop();

      MonitoredStopVisit.RecordedAtTime = new GregorianCalendar();
      MonitoredStopVisit.RecordedAtTime.setTimeInMillis(status.getLastUpdateTime());
      MonitoredStopVisit.RecordedAtTime.setTimeInMillis(status.getLastUpdateTime());

      MonitoredStopVisit.MonitoringRef = SiriUtils.getIdWithoutAgency(adbean.getStopId());
      MonitoredStopVisit.MonitoredVehicleJourney = new MonitoredVehicleJourney();

      String routeIdNoAgency = SiriUtils.getIdWithoutAgency(route.getId());

      MonitoredStopVisit.MonitoredVehicleJourney.LineRef = routeIdNoAgency;
      MonitoredStopVisit.MonitoredVehicleJourney.DirectionRef = trip.getDirectionId();
      MonitoredStopVisit.MonitoredVehicleJourney.VehicleRef = status.getVehicleId();
      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef = new FramedVehicleJourneyRef();
      MonitoredCall monitoredCall = new MonitoredCall();
      MonitoredStopVisit.MonitoredVehicleJourney.MonitoredCall = monitoredCall;
      monitoredCall.Extensions = new DistanceExtensions();
      monitoredCall.StopPointRef = stopId;

      CoordinatePoint position = status.getLocation();
      if (position != null) {
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation = new VehicleLocation();
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Latitude = status.getLocation().getLat();
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Longitude = status.getLocation().getLon();
        double distance = status.getDistanceAlongTrip();
        if (Double.isNaN(distance)) {
          distance = status.getScheduledDistanceAlongTrip();
        }
        monitoredCall.Extensions.DistanceAlongRoute = distance;
        monitoredCall.Extensions.DistanceFromCall = adbean.getDistanceFromStop();
      }

      /* FIXME: get this from api */
      MonitoredStopVisit.MonitoredVehicleJourney.ProgressRate = "normalProgress";

      int i = 0;
      int stopsFromCall = -1;
      boolean started = false;

      List<TripStopTimeBean> stopTimes = specificTripDetails.getSchedule().getStopTimes();

      /*
       * go through every stop in the trip to (a) find out how far many stops
       * away the bus is from this stop and (b) populate, if necessary,
       * onwardCalls
       */
      HashMap<String, Integer> visitNumberForStop = new HashMap<String, Integer>();
      for (TripStopTimeBean stopTime : stopTimes) {
        StopBean stop = stopTime.getStop();
        int visitNumber = SiriUtils.getVisitNumber(visitNumberForStop, stop);
        if (started) {
          i++;
        }
        if (stopTime.getStop().equals(closestStop)) {
          started = true;
        }

        double distance = status.getDistanceAlongTrip();
        if (Double.isNaN(distance)) {
          distance = status.getScheduledDistanceAlongTrip();
        }
        if (stopTime.getDistanceAlongTrip() >= distance) {
          if (stopsFromCall == -1) {
            stopsFromCall = i;
            monitoredCall.VisitNumber = visitNumber;
            if (includeOnwardCalls) {
              List<OnwardCall> onwardCalls = SiriUtils.getOnwardCalls(stopTimes, status.getServiceDate(), stop);
              MonitoredStopVisit.MonitoredVehicleJourney.OnwardCalls = onwardCalls;
              break;
            }
          }
        }
      }
      if (started == false) {
        /* remove trips which have already passed this stop */
        continue;
      }
      monitoredCall.Extensions.StopsFromCall = i;

      Date serviceDate = new Date(adbean.getServiceDate());

      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef.DataFrameRef = String.format(
          "%1$tY-%1$tm-%1$td", serviceDate);
      MonitoredStopVisit.MonitoredVehicleJourney.FramedVehicleJourneyRef.DatedVehicleJourneyRef = trip.getId();
      MonitoredStopVisit.MonitoredVehicleJourney.PublishedLineName = trip.getTripHeadsign();

      MonitoredStopVisit.MonitoredVehicleJourney.OriginRef = SiriUtils.getIdWithoutAgency(stops.get(
          0).getId());
      StopBean lastStop = stops.get(stops.size() - 1);
      MonitoredStopVisit.MonitoredVehicleJourney.DestinationRef = SiriUtils.getIdWithoutAgency(lastStop.getId());

      delivery.visits.add(MonitoredStopVisit);
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
