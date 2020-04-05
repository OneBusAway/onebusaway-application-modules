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
package org.onebusaway.gtfs_realtime.archiver.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.gtfs_realtime.interfaces.FeedEntityModel;
import org.onebusaway.gtfs_realtime.model.StopTimeUpdateModel;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

@Component
public class GtfsRealtimeRetrieverImpl implements GtfsRealtimeRetriever {
  
  private static final String GTFS_RT_VERSION = "1.0"; 
  private static final String GTFS_RT_DATE_FORMAT = "yyyyMMdd";
  private static final String GTFS_RT_TIME_FORMAT = "HH:mm:ss";
  
  @Autowired
  TripUpdateDao _tripUpdateDao;
  
  @Autowired
  VehiclePositionDao _vehiclePositionDao;
  
  @Override  
  public FeedMessage getFeedMessage(EntityType type, Date startDate, Date endDate) {
  
    FeedMessage.Builder builder = FeedMessage.newBuilder();
    
    List<? extends FeedEntityModel> updates = (type == EntityType.VEHICLE) ?
        _vehiclePositionDao.findByDate(startDate, endDate)
        :  _tripUpdateDao.findByDate(startDate, endDate);
 
    long timestamp = 0;

    for (FeedEntityModel update : updates) {
      FeedEntity.Builder fe = fillFeedEntity(update);
      // This is not guaranteed to match entityID in source feed.
      fe.setId(Long.toString(update.getId()));
      builder.addEntity(fe);
      timestamp = Math.max(timestamp, update.getTimestamp().getTime());
    }
    
    FeedHeader.Builder header = FeedHeader.newBuilder();
    header.setTimestamp(timestamp/1000);
    header.setGtfsRealtimeVersion(GTFS_RT_VERSION);
    
    builder.setHeader(header);
    
    return builder.build();
  }
  
  private FeedEntity.Builder fillFeedEntity(FeedEntityModel model) {
    if (model instanceof VehiclePositionModel) {
      return fillFeedEntity((VehiclePositionModel) model);
    }
    else if (model instanceof TripUpdateModel) {
      return fillFeedEntity((TripUpdateModel) model);
    }
    else {
      return null;
    }
  }
  
  
  private FeedEntity.Builder fillFeedEntity(VehiclePositionModel model) {
    VehiclePosition vp = writeVehiclePosition(model);
    return FeedEntity.newBuilder()
        .setVehicle(vp);
  }
  
  private FeedEntity.Builder fillFeedEntity(TripUpdateModel model) {
    TripUpdate tu = writeTripUpdate(model);
    return FeedEntity.newBuilder()
        .setTripUpdate(tu);
  }
  
  private TripUpdate writeTripUpdate(TripUpdateModel model) {
    TripUpdate.Builder tu = TripUpdate.newBuilder();
    TripDescriptor.Builder t = TripDescriptor.newBuilder();
    
    if (model.getDelay() != null) {
      tu.setDelay(model.getDelay());
    }
    if (model.getTripId() != null) {
      t.setTripId(parseId(model.getTripId()));
    }
    if (model.getRouteId() != null) {
      t.setRouteId(parseId(model.getRouteId()));
    }
    if (model.getTripStart() != null) {
      Date date = model.getTripStart();
      String startDate = new SimpleDateFormat(GTFS_RT_DATE_FORMAT).format(date);
      String startTime = new SimpleDateFormat(GTFS_RT_TIME_FORMAT).format(date);
      t.setStartDate(startDate);
      t.setStartTime(startTime);
    }
    
    TripDescriptor.ScheduleRelationship sr = 
        TripDescriptor.ScheduleRelationship.valueOf(model.getScheduleRelationship());
    if (sr != null) {
      t.setScheduleRelationship(sr);
    }
    
    if (model.getVehicleId() != null) {
      VehicleDescriptor.Builder v = VehicleDescriptor.newBuilder();
      v.setId(model.getVehicleId());
      if (!StringUtils.isEmpty(model.getVehicleLabel())) {
        v.setLabel(model.getVehicleLabel());
      }
      if (!StringUtils.isEmpty(model.getVehicleLicensePlate())) {
        v.setLicensePlate(model.getVehicleLicensePlate());
      }
      tu.setVehicle(v.build());
    }
    
    for (StopTimeUpdateModel stum : model.getStopTimeUpdates()) {
        StopTimeUpdate stu = writeStopTimeUpdate(stum);
        if (stum != null) {
          tu.addStopTimeUpdate(stu);
        }
    }
    
    tu.setTrip(t);
    
    return tu.build();
  }
  
  private StopTimeUpdate writeStopTimeUpdate(StopTimeUpdateModel stum) {
    if (stum == null)
      return null;
    StopTimeUpdate.Builder stu = StopTimeUpdate.newBuilder();
    if (stum.getStopSequence() != null) {
      stu.setStopSequence(stum.getStopSequence().intValue());
    }
    if (stum.getStopId() != null) {
      String parsedStopId = parseId(stum.getStopId());
      if (parsedStopId != null) {
        stu.setStopId(parseId(stum.getStopId()));
      } else {
        // parsing failed (invalid agency-and-id) but pass along as is
        stu.setStopId(stum.getStopId());
      }
    }
    
    if (stum.getArrivalDelay() != null || stum.getArrivalTime() != null) {
      StopTimeEvent arrival = writeStopTimeEvent(stum.getArrivalDelay(), stum.getArrivalTime(), stum.getArrivalUncertainty());
      stu.setArrival(arrival);
    }
    
    if (stum.getDepartureDelay() != null || stum.getDepartureTime() != null) {
      StopTimeEvent dept = writeStopTimeEvent(stum.getDepartureDelay(), stum.getDepartureTime(), stum.getDepartureUncertainty());
      stu.setDeparture(dept);
    }
   
    StopTimeUpdate.ScheduleRelationship sr =
        StopTimeUpdate.ScheduleRelationship.valueOf(stum.getScheduleRelationship());
    if (sr != null) {
      stu.setScheduleRelationship(sr);
    }
    
    return stu.build();
  }
  
  private StopTimeEvent writeStopTimeEvent(Integer delay, Date time, Integer uncertainty) {
    StopTimeEvent.Builder ste = StopTimeEvent.newBuilder();
    
    if (delay != null) {
      ste.setDelay(delay);
    }
    
    if (time != null) {
      ste.setTime(time.getTime() / 1000);
    }
    
    if (uncertainty != null) {
      ste.setUncertainty(uncertainty);
    }
    
    return ste.build();
  }

  
  private VehiclePosition writeVehiclePosition(VehiclePositionModel model) {

    VehiclePosition.Builder vp = VehiclePosition.newBuilder();
    
    if (model.getVehicleId() != null) {
      VehicleDescriptor.Builder v = VehicleDescriptor.newBuilder();
      v.setId(model.getVehicleId());
      if (!StringUtils.isEmpty(model.getVehicleLabel())) {
        v.setLabel(model.getVehicleLabel());
      }
      if (!StringUtils.isEmpty(model.getVehicleLicensePlate())) {
        v.setLicensePlate(model.getVehicleLicensePlate());
      }
      vp.setVehicle(v);
    }
      
    Position.Builder p = Position.newBuilder();
    if (model.getBearing() != null) {
      p.setBearing(model.getBearing());
    }
    if (model.getLat() != null) {
      p.setLatitude(model.getLat());
    }
    if (model.getLon() != null) {
      p.setLongitude(model.getLon());
    }
    if (model.getSpeed() != null) {
      p.setSpeed(model.getSpeed());
    }
    vp.setPosition(p);
    
    if (!StringUtils.isEmpty(model.getTripId())) {
      TripDescriptor.Builder td = TripDescriptor.newBuilder();
      td.setTripId(parseId(model.getTripId()));
      if (!StringUtils.isEmpty(model.getRouteId())) {
        td.setRouteId(parseId(model.getRouteId()));
      }
      if (model.getTripStart() != null) {
        Date date = model.getTripStart();
        String startDate = new SimpleDateFormat(GTFS_RT_DATE_FORMAT).format(date);
        String startTime = new SimpleDateFormat(GTFS_RT_TIME_FORMAT).format(date);
        td.setStartDate(startDate);
        td.setStartTime(startTime);
      }
      vp.setTrip(td);
    }
    
    if (!StringUtils.isEmpty(model.getStopId())) {
      vp.setStopId(parseId(model.getStopId()));
    }
   
    return vp.build();
  }
  
  // parse an ID of the form agencyID_ID to ID
  private String parseId(String id) {
    try {
      return AgencyAndIdLibrary.convertFromString(id).getId();
    } catch (IllegalStateException ise) {
      // KCM has technically invalid stop ids (100074234)
      return null;
    }
  }
}
