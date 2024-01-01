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


import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.trips.*;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.text.SimpleDateFormat;
import java.util.*;

public class TripUpdatesForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 2L;

  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp, FILTER_TYPE filterType, String filterValue) {

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
    setLastModifiedHeader(timestamp);
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
    if (activeTrip.getTripHeadsign() != null) {
      // add extension for headsign support
      GtfsRealtimeOneBusAway.OneBusAwayTripUpdate.Builder obaTripUpdate =
              GtfsRealtimeOneBusAway.OneBusAwayTripUpdate.newBuilder();
      obaTripUpdate.setTripHeadsign(activeTrip.getTripHeadsign());
      tripUpdate.setExtension(GtfsRealtimeOneBusAway.obaTripUpdate, obaTripUpdate.build());
    }

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

      TripStopTimesBean schedule = getScheduleForTrip(activeTripId, tripStatus.getServiceDate(),
              tripStatus.getVehicleId(), timestamp);

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

        // add extension for headsign support
        GtfsRealtimeOneBusAway.OneBusAwayTripUpdate.Builder obaTripUpdate =
                GtfsRealtimeOneBusAway.OneBusAwayTripUpdate.newBuilder();
        obaTripUpdate.setTripHeadsign(activeTrip.getTripHeadsign());
        tripUpdate.setExtension(GtfsRealtimeOneBusAway.obaTripUpdate, obaTripUpdate.build());
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
        if (timepointPrediction.getTimepointPredictedArrivalTime() != -1) {
          TripUpdate.StopTimeEvent.Builder arrival = stopTimeUpdate.getArrivalBuilder();
          arrival.setTime(timepointPrediction.getTimepointPredictedArrivalTime() / 1000L);
        }

        if (timepointPrediction.getTimepointPredictedDepartureTime() != -1) {
          TripUpdate.StopTimeEvent.Builder departure = stopTimeUpdate.getDepartureBuilder();
          departure.setTime(timepointPrediction.getTimepointPredictedDepartureTime() / 1000L);
        }
        tripUpdate.setTimestamp(timestamp / 1000);

        String stopHeadsign = getHeadsignForStop(schedule, stopId, timepointPrediction.getStopSequence());

        if (stopHeadsign != null) {
          GtfsRealtimeOneBusAway.OneBusAwayStopTimeUpdate.Builder obaStopTimeUpdate =
                  GtfsRealtimeOneBusAway.OneBusAwayStopTimeUpdate.newBuilder();
          obaStopTimeUpdate.setStopHeadsign(stopHeadsign);
          stopTimeUpdate.setExtension(GtfsRealtimeOneBusAway.obaStopTimeUpdate, obaStopTimeUpdate.build());
        }
      }
    }
    return tripUpdates;
  }

  private TripStopTimesBean getScheduleForTrip(String activeTripId, long serviceDate, String vehicleId, long timestamp) {
    TripDetailsQueryBean query = new TripDetailsQueryBean();
    query.setTripId(activeTripId);
    query.setServiceDate(serviceDate);
    query.setVehicleId(vehicleId);
    query.setTime(timestamp);
    TripDetailsBean tripDetails = _service.getSingleTripDetails(query);
    TripStopTimesBean schedule = null;
    if (tripDetails != null) {
      schedule = tripDetails.getSchedule();
    }
    return schedule;
  }

  private String getHeadsignForStop(TripStopTimesBean schedule, AgencyAndId stopId, int sequence) {
    if (schedule == null) return null;
    if (sequence < schedule.getStopTimes().size() && sequence >= 0) {
      // try a direct sequence lookup
      TripStopTimeBean tripStopTimeBean = schedule.getStopTimes().get(sequence);
      if (tripStopTimeBean.getStop().getId().equals(AgencyAndIdLibrary.convertToString(stopId))) {
        return tripStopTimeBean.getStopHeadsign();
      }
    }
    // sequence didn't index, but perhaps it matches to gtfs sequence
    for (TripStopTimeBean stopTime : schedule.getStopTimes()) {
      if (stopTime.getGtfsSequence() == sequence) {
        if (stopTime.getStop().getId().equals(AgencyAndIdLibrary.convertToString(stopId))) {
          return stopTime.getStopHeadsign();
        }
      }
    }
    // sequence didn't match, find the first stop that matches and hope there are no loops
    for (TripStopTimeBean stopTime : schedule.getStopTimes()) {
      if (stopTime.getStop().getId().equals(AgencyAndIdLibrary.convertToString(stopId))) {
        return stopTime.getStopHeadsign();
      }
    }
    // we fell through -- bad data -- no possible stop headsign
    return null;
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
