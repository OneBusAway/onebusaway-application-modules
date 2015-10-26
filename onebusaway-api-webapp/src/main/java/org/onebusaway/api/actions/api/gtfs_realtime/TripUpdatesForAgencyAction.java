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

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import org.onebusaway.transit_data.model.trips.TimepointPredictionBean;

public class TripUpdatesForAgencyAction extends GtfsRealtimeActionSupport {

  private static final long serialVersionUID = 2L;

  @Override
  protected void fillFeedMessage(FeedMessage.Builder feed, String agencyId,
      long timestamp) {

    ListBean<VehicleStatusBean> vehicles = _service.getAllVehiclesForAgency(
        agencyId, timestamp);

    for (VehicleStatusBean vehicle : vehicles.getList()) {
      TripStatusBean tripStatus = vehicle.getTripStatus();
      if (tripStatus == null) {
        continue;
      }
      TripBean activeTrip = tripStatus.getActiveTrip();
      RouteBean route = activeTrip.getRoute();

      FeedEntity.Builder entity = feed.addEntityBuilder();
      entity.setId(Integer.toString(feed.getEntityCount()));
      TripUpdate.Builder tripUpdate = entity.getTripUpdateBuilder();

      TripDescriptor.Builder tripDesc = tripUpdate.getTripBuilder();
      tripDesc.setTripId(normalizeId(activeTrip.getId()));
      tripDesc.setRouteId(normalizeId(route.getId()));

      VehicleDescriptor.Builder vehicleDesc = tripUpdate.getVehicleBuilder();
      vehicleDesc.setId(normalizeId(vehicle.getVehicleId()));

      if (tripStatus.getTimepointPredictions() != null && tripStatus.getTimepointPredictions().size() > 0) {
        for (TimepointPredictionBean timepointPrediction: tripStatus.getTimepointPredictions()) {
          TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = tripUpdate.addStopTimeUpdateBuilder();
          stopTimeUpdate.setStopId(normalizeId(timepointPrediction.getTimepointId()));
          TripUpdate.StopTimeEvent.Builder arrival = stopTimeUpdate.getArrivalBuilder();
          if (timepointPrediction.getTimepointPredictedArrivalTime() != -1) {
            arrival.setTime(timepointPrediction.getTimepointPredictedArrivalTime());
          }
  
          TripUpdate.StopTimeEvent.Builder departure = stopTimeUpdate.getDepartureBuilder();
          if (timepointPrediction.getTimepointPredictedDepartureTime() != -1) {
            departure.setTime(timepointPrediction.getTimepointPredictedDepartureTime());
          }

	      
        }
        
        tripUpdate.setTimestamp(vehicle.getLastUpdateTime() / 1000);
      } else {
        StopBean nextStop = tripStatus.getNextStop();
        if (nextStop != null) {
          TripUpdate.StopTimeUpdate.Builder stopTimeUpdate = tripUpdate.addStopTimeUpdateBuilder();
          stopTimeUpdate.setStopId(normalizeId(nextStop.getId()));
          TripUpdate.StopTimeEvent.Builder departure = stopTimeUpdate.getDepartureBuilder();
          departure.setTime(timestamp / 1000 + tripStatus.getNextStopTimeOffset());
        }
        
      }
      tripUpdate.setDelay((int) tripStatus.getScheduleDeviation());
      tripUpdate.setTimestamp(vehicle.getLastUpdateTime() / 1000);
    }
  }
}
