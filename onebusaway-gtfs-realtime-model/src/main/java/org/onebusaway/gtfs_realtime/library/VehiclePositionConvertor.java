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

import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;

public class VehiclePositionConvertor extends FeedEntityConvertor<VehiclePositionModel>{

  @Override
  public VehiclePositionModel readFeedEntity(FeedEntity entity,
      long timestamp) {

    if (entity == null)
      return null;
    VehiclePositionModel vpm = new VehiclePositionModel();
    if (entity.hasVehicle()) {
      if (entity.getVehicle().hasTimestamp()) {
        timestamp = entity.getVehicle().getTimestamp() * 1000;
      }
      if (entity.getVehicle().hasTrip()) {
        TripDescriptor td = entity.getVehicle().getTrip();
        if (td.hasTripId()) {
          vpm.setTripId(td.getTripId());
        }
        if (td.hasRouteId()) {
          vpm.setRouteId(td.getRouteId());
        }
        if (td.hasStartDate() && td.hasStartTime()) {
          vpm.setTripStart(GtfsRealtimeConversionLibrary.parseDate(td.getStartDate(), td.getStartTime()));
        }
      }
      if (entity.getVehicle().hasVehicle()) {
        VehicleDescriptor vd = entity.getVehicle().getVehicle();
        if (vd.hasId()) {
          vpm.setVehicleId(vd.getId());
        }
        if (vd.hasLabel()) {
          vpm.setVehicleLabel(vd.getLabel());
        }
        if (vd.hasLicensePlate()) {
          vpm.setVehicleLicensePlate(vd.getLicensePlate());
        }
      }
      if (entity.getVehicle().hasPosition()) {
        Position p = entity.getVehicle().getPosition();
        vpm.setLat(p.getLatitude());
        vpm.setLon(p.getLongitude());
        if (p.hasBearing()) {
          vpm.setBearing(p.getBearing());
        }
        if (p.hasSpeed()) {
          vpm.setSpeed(p.getSpeed());
        }
      }
      if (entity.getVehicle().hasTrip()) {
        if (vpm.getTripId() == null) {
          if (entity.getVehicle().getTrip().hasTripId()) {
            vpm.setTripId(entity.getVehicle().getTrip().getTripId());
          }
        }
        if (vpm.getRouteId() == null) {
          if (entity.getVehicle().getTrip().hasRouteId()) {
            vpm.setRouteId(entity.getVehicle().getTrip().getRouteId());
          }
        }
      }
    }
    if (entity.getVehicle().hasStopId()) {
      vpm.setStopId(entity.getVehicle().getStopId());
    }
    vpm.setTimestamp(new Date(timestamp));
    return vpm;
  }


}
