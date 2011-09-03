/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.api.actions.siri;

import org.onebusaway.api.actions.OneBusAwayApiActionSupport;
import org.onebusaway.siri.model.MonitoredVehicleJourney;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.VehicleActivity;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.siri.model.VehicleMonitoringDelivery;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

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
 * For a given vehicle or set of vehicles, returns the location. Can select
 * vehicles by id, trip, or route.
 */
public class VehicleMonitoringAction extends OneBusAwayApiActionSupport
    implements ModelDriven<Object>, ServletRequestAware {

  private static final long serialVersionUID = 1L;

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
   * This is the default action, corresponding to a SIRI
   * VehicleMonitoringRequest
   * 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {

    String agencyId = _request.getParameter("OperatorRef");

    if (_time == null)
      _time = new Date();

    String detailLevel = _request.getParameter("VehicleMonitoringDetailLevel");
    boolean onwardCalls = false;
    if (detailLevel != null) {
      onwardCalls = detailLevel.equals("calls");
    }

    String vehicleId = _request.getParameter("VehicleRef");

    // single trip, by vehicle
    if (vehicleId != null) {
      String vehicleIdWithAgency = agencyId + "_" + vehicleId;
      VehicleStatusBean vehicle = _transitDataService.getVehicleForAgency(
          vehicleIdWithAgency, _time.getTime());
      ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
      if (vehicle != null) {
        if (!(vehicle.getPhase().equals("DEADHEAD_AFTER")
            || vehicle.getPhase().equals("DEADHEAD_BEFORE") || vehicle.getPhase().equals(
            "DEADHEAD_DURING"))) {
          activities.add(createActivity(vehicle, onwardCalls));
        }
      }
      _response = generateSiriResponse(_time, activities);
      return new DefaultHttpHeaders();
    }

    String directionId = _request.getParameter("DirectionRef");

    // by trip (may be more than one trip)
    String tripId = _request.getParameter("VehicleJourneyRef");
    if (tripId != null) {
      TripBean tripBean = _transitDataService.getTrip(agencyId + "_" + tripId);
      if (tripBean == null) {
        throw new IllegalArgumentException("No such trip: " + tripId);
      }
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTripId(tripId);
      ListBean<TripDetailsBean> trips = _transitDataService.getTripDetails(query);
      ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
      for (TripDetailsBean trip : trips.getList()) {
        if (directionId != null
            && !trip.getTrip().getDirectionId().equals(directionId)) {
          continue;
        }
        if (trip.getStatus().isPredicted() == false) {
          /* only show trips with realtime info */
          continue;
        }
        VehicleActivity activity = createActivity(trip, onwardCalls);
        if (activity != null) {
          activities.add(activity);
        }
      }
      _response = generateSiriResponse(_time, activities);
      return new DefaultHttpHeaders();
    }

    String routeId = _request.getParameter("LineRef");
    // multiple trips by route
    if (routeId != null) {
      TripsForRouteQueryBean query = new TripsForRouteQueryBean();
      query.setRouteId(agencyId + "_" + routeId);
      query.setTime(_time.getTime());
      ListBean<TripDetailsBean> trips = _transitDataService.getTripsForRoute(query);
      ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
      for (TripDetailsBean trip : trips.getList()) {
        if (directionId != null
            && !trip.getTrip().getDirectionId().equals(directionId)) {
          continue;
        }
        if (trip.getStatus().isPredicted() == false) {
          /* only show trips with realtime info */
          continue;
        }
        VehicleActivity activity = createActivity(trip, onwardCalls);
        if (activity != null) {
          activities.add(activity);
        }
      }
      _response = generateSiriResponse(_time, activities);
      return new DefaultHttpHeaders();
    }

    /* All vehicles */
    ListBean<VehicleStatusBean> vehicles = _transitDataService.getAllVehiclesForAgency(
        agencyId, _time.getTime());
    ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
    for (VehicleStatusBean v : vehicles.getList()) {
      VehicleActivity activity = createActivity(v, onwardCalls);
      if (activity != null) {
        activities.add(activity);
      }
    }
    _response = generateSiriResponse(_time, activities);
    return new DefaultHttpHeaders();
  }

  private VehicleActivity createActivity(VehicleStatusBean vehicleStatus,
      boolean onwardCalls) {

    if (vehicleStatus.getPhase().equals("DEADHEAD_AFTER")
        || vehicleStatus.getPhase().equals("DEADHEAD_BEFORE")
        || vehicleStatus.getPhase().equals("DEADHEAD_DURING")) {
      return null;
    }
    VehicleActivity activity = new VehicleActivity();

    Calendar time = Calendar.getInstance();
    time.setTime(new Date(vehicleStatus.getLastUpdateTime()));

    activity.RecordedAtTime = time;
    TripBean tripBean = vehicleStatus.getTrip();
    if (tripBean != null) {
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTime(time.getTimeInMillis());
      query.setTripId(tripBean.getId());
      query.setVehicleId(vehicleStatus.getVehicleId());
      query.getInclusion().setIncludeTripStatus(true);
      TripStatusBean tripStatus = vehicleStatus.getTripStatus();
      query.setServiceDate(tripStatus.getServiceDate());
      TripDetailsBean tripDetails = _transitDataService.getSingleTripDetails(query);
      activity.MonitoredVehicleJourney = SiriUtils.getMonitoredVehicleJourney(
          tripDetails, new Date(tripStatus.getServiceDate()),
          vehicleStatus.getVehicleId());

      if (onwardCalls) {

        List<TripStopTimeBean> stopTimes = tripDetails.getSchedule().getStopTimes();

        long serviceDateMillis = tripStatus.getServiceDate();
        double distance = tripStatus.getDistanceAlongTrip();
        if (Double.isNaN(distance)) {
          distance = tripStatus.getScheduledDistanceAlongTrip();
        }
        activity.MonitoredVehicleJourney.OnwardCalls = SiriUtils.getOnwardCalls(
            stopTimes, serviceDateMillis, distance, tripStatus.getNextStop());
      }
    } else {
      activity.MonitoredVehicleJourney = new MonitoredVehicleJourney();
    }
    activity.MonitoredVehicleJourney.Monitored = true;

    activity.MonitoredVehicleJourney.VehicleRef = vehicleStatus.getVehicleId();

    activity.MonitoredVehicleJourney.ProgressRate = SiriUtils.getProgressRateForStatus(vehicleStatus.getStatus());

    VehicleLocation location = new VehicleLocation();
    location.Latitude = vehicleStatus.getLocation().getLat();
    location.Longitude = vehicleStatus.getLocation().getLon();

    activity.MonitoredVehicleJourney.VehicleLocation = location;
    return activity;
  }

  /** Generate a siri response for a set of VehicleActivities */
  private Siri generateSiriResponse(Date time,
      ArrayList<VehicleActivity> activities) {
    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(time);
    siri.ServiceDelivery.ResponseTimestamp = calendar;

    siri.ServiceDelivery.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ResponseTimestamp = siri.ServiceDelivery.ResponseTimestamp;

    siri.ServiceDelivery.VehicleMonitoringDelivery.ValidUntil = (Calendar) calendar.clone();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ValidUntil.add(
        Calendar.MINUTE, 1);

    siri.ServiceDelivery.VehicleMonitoringDelivery.deliveries = activities;
    return siri;
  }

  /**
   * Create a VehicleActivity for a given vehicle's trip.
   */
  private VehicleActivity createActivity(TripDetailsBean trip,
      boolean onwardCalls) {
    VehicleActivity activity = new VehicleActivity();
    TripStatusBean status = trip.getStatus();
    if (status.getPhase().equals("DEADHEAD_AFTER")
        || status.getPhase().equals("DEADHEAD_BEFORE")
        || status.getPhase().equals("DEADHEAD_DURING")) {
      return null;
    }

    Calendar time = Calendar.getInstance();
    time.setTime(new Date(status.getLastUpdateTime()));

    activity.RecordedAtTime = time;

    activity.MonitoredVehicleJourney = SiriUtils.getMonitoredVehicleJourney(
        trip, new Date(status.getServiceDate()), status.getVehicleId());
    activity.MonitoredVehicleJourney.Monitored = true;
    activity.MonitoredVehicleJourney.VehicleRef = status.getVehicleId();

    activity.MonitoredVehicleJourney.ProgressRate = status.getStatus();

    VehicleLocation location = new VehicleLocation();
    location.Latitude = status.getLocation().getLat();
    location.Longitude = status.getLocation().getLon();

    activity.MonitoredVehicleJourney.VehicleLocation = location;

    if (onwardCalls) {
      List<TripStopTimeBean> stopTimes = trip.getSchedule().getStopTimes();

      long serviceDateMillis = status.getServiceDate();
      double distance = status.getDistanceAlongTrip();
      if (Double.isNaN(distance)) {
        distance = status.getScheduledDistanceAlongTrip();
      }
      activity.MonitoredVehicleJourney.OnwardCalls = SiriUtils.getOnwardCalls(
          stopTimes, serviceDateMillis, distance, status.getNextStop());
    }

    return activity;
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
