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

import org.onebusaway.siri.model.ErrorMessage;
import org.onebusaway.siri.model.ServiceDelivery;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.VehicleActivity;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.siri.model.VehicleMonitoringDelivery;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
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
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

/**
 * For a given vehicle or set of vehicles, returns the location. Can select
 * vehicles by id, trip, or route.
 */
public class VehicleMonitoringController implements ModelDriven<Object>,
    ServletRequestAware {

  private Object _response;
  private HttpServletRequest _request;
  
  @Autowired  
  private TransitDataService _transitDataService;
  private TimeZone defaultTimeZone = TimeZone.getTimeZone("America/New York");

  /**
   * This is the default action
   * 
   * @return
   * @throws IOException
   */
  public DefaultHttpHeaders index() throws IOException {
    
    String agencyId = _request.getParameter("OperatorRef");
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

    String detailLevel = _request.getParameter("VehicleMonitoringDetailLevel");
    boolean onwardCalls = false;
    if (detailLevel != null) {
      onwardCalls = detailLevel.equals("calls");
    }

    String vehicleId = _request.getParameter("VehicleRef");

    // single trip, by vehicle
    if (vehicleId != null) {
      TripDetailsBean trip = getTripForVehicle(agencyId, vehicleId, timeZone);

      if (trip == null) {
        /*
         * FIXME: This vehicle isn't on a trip. In the future, we'll correctly
         * return a just-location trip.
         */
        _response = new ErrorMessage("No known trip for this vehicle");
        return new DefaultHttpHeaders();
      }

      return singleVehicleTrip(timeZone, onwardCalls, trip);
    }

    Calendar now = Calendar.getInstance(timeZone);

    String directionId = _request.getParameter("DirectionRef");

    // single trip, by trip (FIXME: there might be more than one!)
    String tripId = _request.getParameter("VehicleJourneyRef");
    if (tripId != null) {
      TripBean trip = _transitDataService.getTrip(agencyId + "_" + tripId);
      if (trip == null) {
        throw new IllegalArgumentException("No such trip: " + tripId);
      }
      TripDetailsQueryBean query = new TripDetailsQueryBean();
      query.setTripId(tripId);
      TripDetailsBean tripDetails = _transitDataService.getSpecificTripDetails(query);
      if (directionId != null
          && tripDetails.getTrip().getDirectionId().equals(directionId)) {
        return singleVehicleTrip(timeZone, onwardCalls, tripDetails);
      } else {

        Siri siri = generateSiriResponse(now, new ArrayList<VehicleActivity>());

        _response = siri;
        return new DefaultHttpHeaders();
      }
    }

    String routeId = _request.getParameter("LineRef");
    // multiple trips by route
    if (routeId != null) {
      TripsForRouteQueryBean query = new TripsForRouteQueryBean();
      query.setRouteId(agencyId + "_" + routeId);
      query.setTime(now.getTimeInMillis());
      ListBean<TripDetailsBean> trips = _transitDataService.getTripsForRoute(query);
      ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
      for (TripDetailsBean trip : trips.getList()) {
        VehicleActivity activity = createActivity(trip, onwardCalls);
        activities.add(activity);
      }
      _response = generateSiriResponse(now, activities);

      return new DefaultHttpHeaders();
    }

    /* FIXME: need api call for all vehicles */

    return new DefaultHttpHeaders();
  }

  /** Generate a siri response for a set of VehicleActivities */
  private Siri generateSiriResponse(Calendar now,
      ArrayList<VehicleActivity> activities) {
    Siri siri = new Siri();
    siri.ServiceDelivery = new ServiceDelivery();
    siri.ServiceDelivery.ResponseTimestamp = now;

    siri.ServiceDelivery.VehicleMonitoringDelivery = new VehicleMonitoringDelivery();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ResponseTimestamp = siri.ServiceDelivery.ResponseTimestamp;
    
    siri.ServiceDelivery.VehicleMonitoringDelivery.ValidUntil = (Calendar) now.clone();
    siri.ServiceDelivery.VehicleMonitoringDelivery.ValidUntil.add(Calendar.MINUTE, 1);

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
    
    Calendar time = Calendar.getInstance();
    time.setTime(new Date(status.getLastUpdateTime()));
    
    activity.RecordedAtTime = time;
    activity.MonitoredVehicleJourney = SiriUtils.getMonitoredVehicleJourney(trip);
    activity.MonitoredVehicleJourney.VehicleRef = status.getVehicleId();

    /* FIXME: get this from api */
    activity.MonitoredVehicleJourney.ProgressRate = "normalProgress";

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
          stopTimes, serviceDateMillis, distance, null);
    }
    
    return activity;
  }

  /** Generate a response for a single vehicle */
  private DefaultHttpHeaders singleVehicleTrip(TimeZone timeZone,
      boolean onwardCalls, TripDetailsBean trip) {
    Calendar now = Calendar.getInstance(timeZone);

    VehicleActivity activity = createActivity(trip, onwardCalls);

    ArrayList<VehicleActivity> activities = new ArrayList<VehicleActivity>();
    activities.add(activity);
    Siri siri = generateSiriResponse(now, activities);

    _response = siri;
    return new DefaultHttpHeaders();
  }

  private TripDetailsBean getTripForVehicle(String agencyId, String vehicleId,
      TimeZone timeZone) {
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
    return trip;
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
