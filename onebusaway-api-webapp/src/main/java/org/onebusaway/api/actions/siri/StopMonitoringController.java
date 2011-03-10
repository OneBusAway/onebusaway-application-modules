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

import org.onebusaway.geospatial.model.CoordinatePoint;
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

    for (ArrivalAndDepartureBean adbean : stopWithArrivalsAndDepartures.getArrivalsAndDepartures()) {
      double distanceFromStop = adbean.getDistanceFromStop();
      if (distanceFromStop < 0) {
        /* passed this stop */
        continue;
      }
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
      query.setVehicleId(adbean.getVehicleId());

      ListBean<TripDetailsBean> trips = _transitDataService.getTripDetails(query);
      for (TripDetailsBean specificTripDetails : trips.getList()) {
        MonitoredStopVisit MonitoredStopVisit = new MonitoredStopVisit();

        TripStatusBean status = specificTripDetails.getStatus();
        if (status == null) {
          // this trip has no status. Let's skip it.
          continue;
        }
        if (status.isPredicted() == false) {
          /* only show trips with realtime info */
          continue;
        }

        MonitoredStopVisit.RecordedAtTime = new GregorianCalendar();
        MonitoredStopVisit.RecordedAtTime.setTimeInMillis(status.getLastUpdateTime());
        MonitoredStopVisit.RecordedAtTime.setTimeInMillis(status.getLastUpdateTime());

        MonitoredStopVisit.MonitoredVehicleJourney = SiriUtils.getMonitoredVehicleJourney(
            specificTripDetails, new Date(status.getServiceDate()),
            status.getVehicleId());
        MonitoredStopVisit.MonitoredVehicleJourney.VehicleRef = status.getVehicleId();

        MonitoredCall monitoredCall = new MonitoredCall();
        MonitoredStopVisit.MonitoredVehicleJourney.MonitoredCall = monitoredCall;
        monitoredCall.Extensions = new DistanceExtensions();
        monitoredCall.StopPointRef = SiriUtils.getIdWithoutAgency(stopId);

        CoordinatePoint position = status.getLocation();
        if (position != null) {
          MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation = new VehicleLocation();
          MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Latitude = status.getLocation().getLat();
          MonitoredStopVisit.MonitoredVehicleJourney.VehicleLocation.Longitude = status.getLocation().getLon();
          double distance = status.getDistanceAlongTrip();
          if (Double.isNaN(distance)) {
            distance = status.getScheduledDistanceAlongTrip();
          }
        }

        MonitoredStopVisit.MonitoredVehicleJourney.ProgressRate = SiriUtils.getProgressRateForStatus(status.getStatus());

        int i = 0;
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

          double distance = status.getDistanceAlongTrip();
          if (Double.isNaN(distance)) {
            distance = status.getScheduledDistanceAlongTrip();
          }
          if (stopTime.getDistanceAlongTrip() >= distance) {
            /*
             * this stop time is further along the route than the vehicle is so
             * we will now start counting stops until we hit the requested stop
             */
            started = true;
          }
          if (started && stopTime.getStop().getId().equals(stopId)) {
            /* we have hit the requested stop */
            monitoredCall.VehicleAtStop = stopTime.getDistanceAlongTrip()
                - distance < 10;
            monitoredCall.Extensions.Distances = new Distances();
            monitoredCall.Extensions.Distances.StopsFromCall = i;
            monitoredCall.Extensions.Distances.CallDistanceAlongRoute = stopTime.getDistanceAlongTrip();;
            monitoredCall.Extensions.Distances.DistanceFromCall = distanceFromStop;

            monitoredCall.VisitNumber = visitNumber;
            if (includeOnwardCalls) {
              List<OnwardCall> onwardCalls = SiriUtils.getOnwardCalls(
                  stopTimes, status.getServiceDate(), distance, stop, -1);
              MonitoredStopVisit.MonitoredVehicleJourney.OnwardCalls = onwardCalls;
            }
          }
        }

        if (monitoredCall.Extensions.Distances == null) {
          /* remove trips which have already passed this stop */
          continue;
        }

        delivery.visits.add(MonitoredStopVisit);
      }
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
