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
import org.onebusaway.gtfs_realtime.archiver.model.TripUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;

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
  public List<TripUpdate> readTripUpdates(FeedMessage tripUpdates) {
    List<TripUpdate> updates = new ArrayList<TripUpdate>();
    if (tripUpdates == null) {
      _log.error("nothing to do!");
      return updates;
    }
    List<FeedEntity> entityList = tripUpdates.getEntityList();
    
    for (FeedEntity entity : entityList) {
      long timestamp = tripUpdates.getHeader().getTimestamp() * 1000;
      TripUpdate tripUpdate = readTripUpdate(entity, timestamp);
      if (tripUpdate != null) {
        _persistor.persist(tripUpdate);
        updates.add(tripUpdate);
      }
    }
    return updates;
  }

  private long getStartOfDayInMillis() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    
    return c.getTimeInMillis();
  }



  private TripUpdate readTripUpdate(FeedEntity entity, long timestamp) {
    if (entity.hasTripUpdate()) {
      TripUpdate tu = new TripUpdate();
      TripDescriptor t = entity.getTripUpdate().getTrip();
      tu.setTimestamp(new Date(timestamp));
      tu.setTripId(t.getTripId());
      tu.setTripStart(parseDate(t.getStartDate(), t.getStartTime()));
      tu.setScheduleRelationship(findRelationShip(t));
      VehicleDescriptor vehicle = entity.getTripUpdate().getVehicle();
      if (vehicle != null) {
        tu.setVehicleId(vehicle.getId());
        tu.setVehicleLabel(vehicle.getLabel());
        tu.setVehicleLicensePlate(vehicle.getLicensePlate());
      }
      return tu;
    }
    return null;
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

}
