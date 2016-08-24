/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.library;

import java.util.Date;

import org.onebusaway.gtfs_realtime.model.StopTimeUpdateModel;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;

public class TripUpdateConvertor extends FeedEntityConvertor<TripUpdateModel> {

  private static final Logger _log = LoggerFactory.getLogger(TripUpdateConvertor.class);
  
  @Override
  public TripUpdateModel readFeedEntity(FeedEntity entity, long timestamp) {
    if (entity.hasTripUpdate()) {
      TripUpdateModel tu = new TripUpdateModel();
      TripDescriptor t = entity.getTripUpdate().getTrip();
      if (entity.getTripUpdate().hasTimestamp()) {
        timestamp = entity.getTripUpdate().getTimestamp() * 1000;
      }
      tu.setTimestamp(new Date(timestamp));
      if (entity.getTripUpdate().hasDelay()) {
        tu.setDelay(entity.getTripUpdate().getDelay());
      }
      if (t.hasTripId())
        tu.setTripId(t.getTripId());
      if (t.hasRouteId()) {
        tu.setRouteId(t.getRouteId());
      }
      if (t.hasStartDate() && t.hasStartTime()) {
        tu.setTripStart(GtfsRealtimeConversionLibrary.parseDate(t.getStartDate(), t.getStartTime()));
      }
      tu.setScheduleRelationship(findRelationship(t));
      VehicleDescriptor vehicle = entity.getTripUpdate().getVehicle();
      if (vehicle != null) {
        tu.setVehicleId(vehicle.getId());
        tu.setVehicleLabel(vehicle.getLabel());
        tu.setVehicleLicensePlate(vehicle.getLicensePlate());
      }

      for (StopTimeUpdate stu : entity.getTripUpdate().getStopTimeUpdateList()) {
        StopTimeUpdateModel stopTimeUpdate = readStopTimeUpdate(stu,
            tu.getScheduleRelationship());
        if (stopTimeUpdate != null) {
          stopTimeUpdate.setTripUpdateModel(tu);
          tu.addStopTimeUpdateModel(stopTimeUpdate);
        }
      }
      return tu;
    }
    return null;
  }

  private static StopTimeUpdateModel readStopTimeUpdate(StopTimeUpdate stu,
      int scheduleRelationship) {
    if (stu == null)
      return null;
    StopTimeUpdateModel stum = new StopTimeUpdateModel();
    if (stu.hasStopSequence()) {
      stum.setStopSequence(stu.getStopSequence());
    }
    if (stu.hasStopId()) {
      stum.setStopId(stu.getStopId());
    }
    if (stu.getArrival() != null) {
      // flatten stop time event
      if (stu.getArrival().hasDelay()) {
        stum.setArrivalDelay(stu.getArrival().getDelay());
      }
      if (stu.getArrival().hasTime()) {
        stum.setArrivalTime(new Date(stu.getArrival().getTime() * 1000));
      }
      if (stu.getArrival().hasUncertainty()) {
        stum.setArrivalUncertainty(stu.getArrival().getUncertainty());
      }
    }
    if (stu.getDeparture() != null) {
      if (stu.getDeparture().hasDelay()) {
        stum.setDepartureDelay(stu.getDeparture().getDelay());
      }
      if (stu.getDeparture().hasTime()) {
        stum.setDepartureTime(new Date(stu.getDeparture().getTime() * 1000));
      }
      if (stu.getDeparture().hasUncertainty()) {
        stum.setDepartureUncertainty(stu.getDeparture().getUncertainty());
      }
    }
    stum.setScheduleRelationship(scheduleRelationship);

    return stum;
  }

  private static int findRelationship(TripDescriptor t) {
    return t.getScheduleRelationship().getNumber();
  }

}
