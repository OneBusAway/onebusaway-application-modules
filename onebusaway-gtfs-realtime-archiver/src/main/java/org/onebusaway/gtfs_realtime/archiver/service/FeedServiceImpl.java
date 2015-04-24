/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.gtfs_realtime.archiver.model.StopTimeUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

@Component
/**
 * Maps the GTFS-realtime protocol buffer models to the archiver models.
 * 
 */
public class FeedServiceImpl implements FeedService {

  private static Logger _log = LoggerFactory.getLogger(FeedServiceImpl.class);
  
  private GtfsPersistor _persistor;

  @Autowired
  public void setGtfsPersistor(GtfsPersistor persistor) {
    _persistor = persistor;
  }
  
  
  @Override
  public List<TripUpdateModel> readTripUpdates(FeedMessage tripUpdates) {
    List<TripUpdateModel> updates = new ArrayList<TripUpdateModel>();
    if (tripUpdates == null) {
      _log.error("nothing to do!");
      return updates;
    }
    List<FeedEntity> entityList = tripUpdates.getEntityList();
    long timestamp = tripUpdates.getHeader().getTimestamp() * 1000;
    
    for (FeedEntity entity : entityList) {
      TripUpdateModel tripUpdate = readTripUpdate(entity, timestamp);
      if (tripUpdate != null) {
        _persistor.persist(tripUpdate);
        updates.add(tripUpdate);
      }
    }
    return updates;
  }

  private TripUpdateModel readTripUpdate(FeedEntity entity, long timestamp) {
    if (entity.hasTripUpdate()) {
      TripUpdateModel tu = new TripUpdateModel();
      TripDescriptor t = entity.getTripUpdate().getTrip();
      tu.setTimestamp(new Date(timestamp));
      if (t.hasTripId())
        tu.setTripId(t.getTripId());
      if (t.hasRouteId()) {
        tu.setRouteId(t.getRouteId());
      }
      if (t.hasStartDate() && t.hasStartTime()) {
        tu.setTripStart(parseDate(t.getStartDate(), t.getStartTime()));
      }
      tu.setScheduleRelationship(findRelationShip(t));
      VehicleDescriptor vehicle = entity.getTripUpdate().getVehicle();
      if (vehicle != null) {
        tu.setVehicleId(vehicle.getId());
        tu.setVehicleLabel(vehicle.getLabel());
        tu.setVehicleLicensePlate(vehicle.getLicensePlate());
      }
      
      for (StopTimeUpdate stu : entity.getTripUpdate().getStopTimeUpdateList()) {
        StopTimeUpdateModel stopTimeUpdate = readStopTimeUpdate(stu, tu.getScheduleRelationship());
        if (stopTimeUpdate != null) {
          stopTimeUpdate.setTripUpdateModel(tu);
          tu.addStopTimeUpdateModel(stopTimeUpdate);
        }
      }
      
      return tu;
    }
    return null;
  }

  private StopTimeUpdateModel readStopTimeUpdate(StopTimeUpdate stu, int scheduleRelationship) {
    if (stu == null) return null;
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
        stum.setArrivalTime(new Date(stu.getArrival().getTime()*1000));
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
        stum.setDepartureTime(new Date(stu.getDeparture().getTime()*1000));
      }
      if (stu.getDeparture().hasUncertainty()) {
        stum.setDepartureUncertainty(stu.getDeparture().getUncertainty());
      }
    }
    stum.setScheduleRelationship(scheduleRelationship);
    
    return stum;
  }


  private int findRelationShip(TripDescriptor t) {
    return t.getScheduleRelationship().getNumber();
  }

  private Date parseDate(String startDate, String startTime) {
    if (StringUtils.isNotBlank(startDate) || StringUtils.isNotBlank(startTime)) {
      _log.info("todo parseDate(" + startDate + "," + startTime + ")");
    }
    return null;
  }


  @Override
  public List<VehiclePositionModel> readVehiclePositions(
      FeedMessage vehiclePositions) {
    List<VehiclePositionModel> models = new ArrayList<VehiclePositionModel>();
    if (vehiclePositions == null) return models;
    long timestamp = vehiclePositions.getHeader().getTimestamp() * 1000;
    for (FeedEntity entity : vehiclePositions.getEntityList()) {
      VehiclePositionModel vehiclePosition = readVehiclePosition(entity, timestamp);
      if (vehiclePosition != null) {
        models.add(vehiclePosition);
        _persistor.persist(vehiclePosition);
      }
    }
    return models;
  }


  private VehiclePositionModel readVehiclePosition(FeedEntity entity, long timestamp) {
    if (entity == null) return null;
    VehiclePositionModel vpm = new VehiclePositionModel();
    if (entity.hasTripUpdate()) {
      if (entity.getTripUpdate().hasTrip()) {
        TripDescriptor td = entity.getTripUpdate().getTrip();
        if (td.hasTripId()) {
          vpm.setTripId(td.getTripId());
        }
        if (td.hasRouteId()) {
          vpm.setRouteId(td.getRouteId());
        }
        if (td.hasStartDate() && td.hasStartTime()) {
          this.parseDate(td.getStartDate(), td.getStartTime());
        }
      }
    }
    if (entity.hasVehicle()) {
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
    vpm.setTimestamp(new Date(timestamp));
    return vpm;
  }

}
