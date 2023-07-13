/**
 * Copyright (C) 2013 Google, Inc.
 * Copyright (C) 2015 University of South Florida
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
package org.onebusaway.api.actions.api.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.apache.struts2.ServletActionContext;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.trips.*;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import org.onebusaway.util.AgencyAndIdLibrary;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

public class TripUpdatesForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 2L;

  private static final SimpleDateFormat _sdf;

  static {
    _sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' ", Locale.US);
    _sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp, FILTER_TYPE filterType, String filterValue) {

    long feedTimestamp = feed.getHeader().getTimestamp();
    ListBean<VehicleStatusBean> vehicles = _service.getAllVehiclesForAgency(
        agencyId, timestamp);

    for (VehicleStatusBean vehicle : vehicles.getList()) {
      TripStatusBean tripStatus = vehicle.getTripStatus();
      if (tripStatus == null) {
        continue;
      }
      TripBean activeTrip = tripStatus.getActiveTrip();
      RouteBean route = activeTrip.getRoute();

      if (FILTER_TYPE.ROUTE_ID == filterType && !filterValue.equals(AgencyAndIdLibrary.convertFromString(route.getId()).getId())) {
        // skip this route
        continue;
      }

      List<TripUpdate.Builder> tripUpdates = new ArrayList<>();
      if (tripStatus.getTimepointPredictions() != null && !tripStatus.getTimepointPredictions().isEmpty()) {
        // use the predictions that we fed to us, not the trivial trip delay propagation
        // also support multiple tripUpdates on the block, not just the active trip
        tripUpdates.addAll(serveTripUpdatesFromTimepoints(agencyId, tripStatus, feed, timestamp));
      } else {
        // we still support legacy trip delay propagation
        tripUpdates.add(serveTripUpdatesFromStatus(agencyId, tripStatus, feed, timestamp));
      }

      for (TripUpdate.Builder tripUpdate : tripUpdates) {
        // we add a vehicle descriptor to each update
        // this may wrong for downstream trips
        tripUpdate.setTimestamp(vehicle.getLastUpdateTime() / 1000);
        VehicleDescriptor.Builder vehicleDesc = tripUpdate.getVehicleBuilder();
        vehicleDesc.setId(normalizeId(vehicle.getVehicleId()));
      }
    }
    if(feedTimestamp != 0){
      long lastModifiedMills = feedTimestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));
      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    } else {
      long lastModifiedMills = timestamp * 1000L;
      String lastModifiedHeader = _sdf.format(new Date(lastModifiedMills));

      HttpServletResponse response = ServletActionContext.getResponse();
      response.setHeader("Last-Modified", lastModifiedHeader);
    }
    addCancelledTrips(agencyId, feed, timestamp);
  }

  private void addCancelledTrips(String agencyId, FeedMessage.Builder feed, long timestamp) {
    TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
    query.setAgencyId(agencyId);
    query.setTime(timestamp);
    ListBean<TripDetailsBean> tripsForAgency = _service.getTripsForAgency(query);
    for (TripDetailsBean tripDetailsBean : tripsForAgency.getList()) {
      if (tripDetailsBean.getStatus() != null) {
        String status = tripDetailsBean.getStatus().getStatus();
        if (!"default".equals(status)) {
          System.out.println("status=" + status);
        }
        if (TransitDataConstants.STATUS_CANCELED.equals(tripDetailsBean.getStatus().getStatus())) {
          FeedEntity.Builder entity = feed.addEntityBuilder();

          // make the id something meaningful and distinct
          entity.setId(tripDetailsBean.getTripId() + "_" + timestamp);
          TripUpdate.Builder tripUpdate = entity.getTripUpdateBuilder();
          TripDescriptor.Builder tripDesc = tripUpdate.getTripBuilder();
          tripDesc.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
          tripDesc.setTripId(normalizeId(tripDetailsBean.getTripId()));
          tripDesc.setStartDate(formatStartDate(tripDetailsBean.getServiceDate()));
          RouteBean route = tripDetailsBean.getTrip().getRoute();
          tripDesc.setRouteId(normalizeId(route.getId()));
        }
      }
    }

  }

  private String formatStartDate(long serviceDate) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    return sdf.format(new Date(serviceDate));
  }

  private TripUpdate.Builder serveTripUpdatesFromStatus(String agencyId, TripStatusBean tripStatus, FeedMessage.Builder feed, long timestamp) {

    TripBean activeTrip = tripStatus.getActiveTrip();

    FeedEntity.Builder entity = feed.addEntityBuilder();
    // make the id something meaningful and distinct
    entity.setId(activeTrip.getId() + "_" + timestamp);
    TripUpdate.Builder tripUpdate = entity.getTripUpdateBuilder();

    TripDescriptor.Builder tripDesc = tripUpdate.getTripBuilder();
    tripDesc.setTripId(normalizeId(activeTrip.getId()));
    RouteBean route = activeTrip.getRoute();
    tripDesc.setRouteId(normalizeId(route.getId()));
    if (TransitDataConstants.STATUS_ADDED.equals(tripStatus.getStatus())) {
      tripDesc.setScheduleRelationship(TripDescriptor.ScheduleRelationship.ADDED);
      tripDesc.setStartDate(formatStartDate(tripStatus.getServiceDate()));
    } else {
      tripDesc.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED);
    }
    StopBean nextStop = tripStatus.getNextStop();
    if (nextStop != null) {
      AgencyAndId stopId = modifiedStopId(agencyId, nextStop.getId());
      if (stopId.getAgencyId().equals(agencyId)) {
        // create the minimal and single stopTimeUpdate using schedule deviation
        TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = tripUpdate.addStopTimeUpdateBuilder();
        stopTimeUpdate.setStopId(normalizeId(stopId.toString()));
        TripUpdate.StopTimeEvent.Builder departure = stopTimeUpdate.getDepartureBuilder();
        departure.setTime(timestamp / 1000 + tripStatus.getNextStopTimeOffset());
      }
    }
    tripUpdate.setDelay((int) tripStatus.getScheduleDeviation());
    return tripUpdate;

  }

  private List<TripUpdate.Builder> serveTripUpdatesFromTimepoints(String agencyId, TripStatusBean tripStatus, FeedMessage.Builder feed, long timestamp) {
    TripBean activeTrip = tripStatus.getActiveTrip();
    List<TripUpdate.Builder> tripUpdates = new ArrayList<>();

    for (String activeTripId : activeTripsIds(tripStatus)) {
      FeedEntity.Builder entity = feed.addEntityBuilder();
      // make the id something meaningful and distinct
      entity.setId(activeTripId + "_" + timestamp);
      TripUpdate.Builder tripUpdate = entity.getTripUpdateBuilder();
      tripUpdates.add(tripUpdate);
      TripDescriptor.Builder tripDesc = tripUpdate.getTripBuilder();
      tripDesc.setTripId(normalizeId(activeTripId));
      if (TransitDataConstants.STATUS_ADDED.equals(tripStatus.getStatus())) {
        tripDesc.setScheduleRelationship(TripDescriptor.ScheduleRelationship.ADDED);
        tripDesc.setStartDate(formatStartDate(tripStatus.getServiceDate()));
        tripDesc.setStartTime(formatStartTime(tripStatus.getServiceDate() + (tripStatus.getTripStartTime() * 1000)));
      } else {
        tripDesc.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED);
      }


      if (activeTripId.equals(activeTrip.getId())) {
        RouteBean route = activeTrip.getRoute();
        // we only know route if we are on activeTrip
        // the block may interline!
        tripDesc.setRouteId(normalizeId(route.getId()));
        tripUpdate.setDelay((int) tripStatus.getScheduleDeviation());
      }

      for (TimepointPredictionBean timepointPrediction : tripStatus.getTimepointPredictions()) {
        if (!timepointPrediction.getTripId().equals(activeTripId)) {
          continue;
        }

        AgencyAndId stopId = modifiedStopId(agencyId, timepointPrediction.getTimepointId());
        TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = tripUpdate.addStopTimeUpdateBuilder();
        /*
         NOTE: here we may serve a stop that belongs to a separate agency and GTFS-RT leaves no way
         * to indicate that
         */
        stopTimeUpdate.setStopId(normalizeId(stopId.toString()));
        TripUpdate.StopTimeEvent.Builder arrival = stopTimeUpdate.getArrivalBuilder();
        if (timepointPrediction.getTimepointPredictedArrivalTime() != -1) {
          arrival.setTime(timepointPrediction.getTimepointPredictedArrivalTime() / 1000L);
        }

        TripUpdate.StopTimeEvent.Builder departure = stopTimeUpdate.getDepartureBuilder();
        if (timepointPrediction.getTimepointPredictedDepartureTime() != -1) {
          departure.setTime(timepointPrediction.getTimepointPredictedDepartureTime() / 1000L);
        }
        tripUpdate.setTimestamp(timestamp / 1000);
      }
    }
    return tripUpdates;
  }

  private String formatStartTime(long l) {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    return sdf.format(new Date(l));
  }


  private List<String> activeTripsIds(TripStatusBean tripStatus) {
    ArrayList<String> activeTrips = new ArrayList<>();
    if (tripStatus.getTimepointPredictions().isEmpty()) {
      activeTrips.add(tripStatus.getActiveTrip().getId());
    } else {
      for (TimepointPredictionBean timepointPrediction : tripStatus.getTimepointPredictions()) {
        if (!activeTrips.contains(timepointPrediction.getTripId())) {
          activeTrips.add(timepointPrediction.getTripId());
        }
      }
    }
    return activeTrips;
  }

}
