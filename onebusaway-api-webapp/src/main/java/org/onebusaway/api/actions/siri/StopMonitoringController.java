/*
 * Copyright 2010, OpenPlans Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onebusaway.api.actions.siri;

import org.onebusaway.api.actions.siri.SiriUtils;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.presentation.model.ArrivalDepartureBeanListFilter;
import org.onebusaway.siri.model.DistanceExtensions;
import org.onebusaway.siri.model.Distances;
import org.onebusaway.siri.model.MonitoredCall;
import org.onebusaway.siri.model.MonitoredStopVisit;
import org.onebusaway.siri.model.OnwardCall;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.StopMonitoringDelivery;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * For a given stop, return the vehicles that are going to stop there soon.
 * Optionally, return the next stops for those vehicles.
 */
public class StopMonitoringController implements ModelDriven<Object>,
    ServletRequestAware {

  private Object _response;
  private HttpServletRequest _request;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private ArrivalDepartureBeanListFilter adbeanFilter;

  private Date _time;

  @TypeConversion(converter = "org.onebusaway.api.actions.siri.Iso8601DateTimeConverter")
  public void setTime(Date time) {
    _time = time;
  }

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

    if (_time == null)
      _time = new Date();

    // convert ids to agency_and_id
    stopId = agencyId + "_" + stopId;

    if (routeId != null) {
      routeId = agencyId + "_" + routeId;
    }
    if (directionId != null) {
      directionId = agencyId + "_" + directionId;
    }

    ArrivalsAndDeparturesQueryBean arrivalsQuery = new ArrivalsAndDeparturesQueryBean();
    arrivalsQuery.setTime(_time.getTime());
    arrivalsQuery.setMinutesBefore(60);
    arrivalsQuery.setMinutesAfter(90);
    arrivalsQuery.setFrequencyMinutesBefore(60);
    arrivalsQuery.setFrequencyMinutesAfter(90);
    StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = _transitDataService.getStopWithArrivalsAndDepartures(
        stopId, arrivalsQuery);

    if (stopWithArrivalsAndDepartures == null) {
      throw new IllegalArgumentException("Bogus stop parameter");
    }

    GregorianCalendar now = new GregorianCalendar();
    now.setTime(_time);

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

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = stopWithArrivalsAndDepartures.getArrivalsAndDepartures();
    arrivalsAndDepartures = adbeanFilter.filter(arrivalsAndDepartures);

    for (ArrivalAndDepartureBean adbean : arrivalsAndDepartures) {
      TripBean trip = adbean.getTrip();
      RouteBean route = trip.getRoute();

      if ((routeId != null && !route.getId().equals(routeId))
          || (directionId != null && !trip.getDirectionId().equals(directionId))) {
        // filtered out
        continue;
      }

      TripStatusBean tripStatus = adbean.getTripStatus();
      String status = null;
      
      if(tripStatus != null) {
    	  status = tripStatus.getStatus();
    	 
    	  if (status.toLowerCase().equals("deviated")) {
    		  continue;
    	  }
      }

      /* gather data about trip, route, and stops */
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTripId(trip.getId());
      query.setServiceDate(adbean.getServiceDate());
      query.setTime(now.getTime().getTime());
      query.setVehicleId(adbean.getVehicleId());

      TripStatusBean tripStatusBean = adbean.getTripStatus();

      ListBean<TripDetailsBean> trips = _transitDataService.getTripDetails(query);
      TripDetailsBean specificTripDetails = null;
      for (TripDetailsBean details : trips.getList()) {
        specificTripDetails = details;
        break;
      }

      if (specificTripDetails == null) {
        continue; /* for some reason, this trip has no schedule info */
      }

      MonitoredStopVisit MonitoredStopVisit = new MonitoredStopVisit();
      MonitoredStopVisit.RecordedAtTime = new GregorianCalendar();
      MonitoredStopVisit.RecordedAtTime.setTimeInMillis(adbean.getLastUpdateTime());
      MonitoredStopVisit.RecordedAtTime.setTimeInMillis(adbean.getLastUpdateTime());

      MonitoredStopVisit.MonitoredVehicleJourney = SiriUtils.getMonitoredVehicleJourney(
          specificTripDetails, new Date(adbean.getServiceDate()),
          adbean.getVehicleId());
      MonitoredStopVisit.MonitoredVehicleJourney.VehicleRef = adbean.getVehicleId();
      MonitoredStopVisit.MonitoredVehicleJourney.ProgressRate = SiriUtils.getProgressRateForStatus(
          adbean.getStatus(), tripStatusBean.getPhase());

      MonitoredCall monitoredCall = new MonitoredCall();
      MonitoredStopVisit.MonitoredVehicleJourney.MonitoredCall = monitoredCall;
      monitoredCall.Extensions = new DistanceExtensions();
      monitoredCall.StopPointRef = SiriUtils.getIdWithoutAgency(stopId);

      CoordinatePoint position = tripStatusBean.getLocation();
      if (position == null) {
        continue;
      }

      MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation = new VehicleLocation();
      MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Latitude = tripStatusBean.getLocation().getLat();
      MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Longitude = tripStatusBean.getLocation().getLon();
      double distance = tripStatusBean.getDistanceAlongTrip();
      if (Double.isNaN(distance)) {
        distance = tripStatusBean.getScheduledDistanceAlongTrip();
      }

      MonitoredStopVisit.MonitoredVehicleJourney.ProgressRate = SiriUtils.getProgressRateForStatus(
          status, tripStatusBean.getPhase());

      monitoredCall.Extensions.Distances = new Distances();
      monitoredCall.Extensions.Distances.StopsFromCall = adbean.getNumberOfStopsAway();
      monitoredCall.Extensions.Distances.CallDistanceAlongRoute = tripStatusBean.getDistanceAlongTrip();
      monitoredCall.Extensions.Distances.DistanceFromCall = adbean.getDistanceFromStop();

      /**/
      boolean started = false;

      List<TripStopTimeBean> stopTimes = specificTripDetails.getSchedule().getStopTimes();
      Collections.sort(stopTimes, new Comparator<TripStopTimeBean>() {
        public int compare(TripStopTimeBean arg0, TripStopTimeBean arg1) {
          return (int) (arg0.getDistanceAlongTrip() - arg1.getDistanceAlongTrip());
        }
      });

      /*
       * go through every stop in the trip to populate OnwardCalls (if
       * requested) and determine visit number
       */
      HashMap<String, Integer> visitNumberForStop = new HashMap<String, Integer>();
      for (TripStopTimeBean stopTime : stopTimes) {
        StopBean stop = stopTime.getStop();
        int visitNumber = SiriUtils.getVisitNumber(visitNumberForStop, stop);
        monitoredCall.VisitNumber = visitNumber;
        if (stopTime.getDistanceAlongTrip() >= distance) {
          /*
           * this stop time is further along the route than the vehicle is so we
           * will now start looking for the requested stop (we don't want to
           * look earlier in case the stop occurs twice)
           */
          started = true;
        }
        if (started && stopTime.getStop().getId().equals(stopId)) {
          /* we have hit the requested stop */
          monitoredCall.VehicleAtStop = Math.abs(tripStatusBean.getNextStopDistanceFromVehicle()
              - distance) < 10;
          if (includeOnwardCalls) {
            List<OnwardCall> onwardCalls = SiriUtils.getOnwardCalls(stopTimes,
                tripStatusBean.getServiceDate(), distance, stop);
            MonitoredStopVisit.MonitoredVehicleJourney.OnwardCalls = onwardCalls;
          }
        }
      }

      delivery.visits.add(MonitoredStopVisit);
    }

    // sort MonitoredStopVisits by distance from stop
    Collections.sort(delivery.visits, new Comparator<MonitoredStopVisit>() {

      @Override
      public int compare(MonitoredStopVisit arg0, MonitoredStopVisit arg1) {
        return (int) (arg0.MonitoredVehicleJourney.MonitoredCall.Extensions.Distances.DistanceFromCall - arg1.MonitoredVehicleJourney.MonitoredCall.Extensions.Distances.DistanceFromCall);
      }

    });

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
