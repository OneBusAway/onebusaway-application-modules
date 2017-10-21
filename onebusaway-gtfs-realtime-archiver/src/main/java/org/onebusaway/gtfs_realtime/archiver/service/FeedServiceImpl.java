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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs_realtime.archiver.listener.GtfsRealtimeEntitySource;
import org.onebusaway.gtfs_realtime.interfaces.HasRouteId;
import org.onebusaway.gtfs_realtime.interfaces.HasStopId;
import org.onebusaway.gtfs_realtime.interfaces.HasTripId;
import org.onebusaway.gtfs_realtime.library.GtfsRealtimeConversionLibrary;
import org.onebusaway.gtfs_realtime.model.AlertModel;
import org.onebusaway.gtfs_realtime.model.EntitySelectorModel;
import org.onebusaway.gtfs_realtime.model.StopTimeUpdateModel;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * Maps the GTFS-realtime protocol buffer models to the archiver models.
 * 
 * Typically, methods will:
 * - use GtfsRealtimeConversionLibrary to convert GTFS-RT to POJO model classes
 * - use entity source to add in data from TDS to models (agency ID)
 * - persist with the GtfsPersistor
 */
@Component
public class FeedServiceImpl implements FeedService {

  private static Logger _log = LoggerFactory.getLogger(FeedServiceImpl.class);

  private GtfsPersistor _persistor;

  @Autowired
  public void setGtfsPersistor(GtfsPersistor persistor) {
    _persistor = persistor;
  }
  
  @Override
  public List<TripUpdateModel> readTripUpdates(FeedMessage tripUpdates,
      GtfsRealtimeEntitySource entitySource) {
  
    List<TripUpdateModel> updates = GtfsRealtimeConversionLibrary.readTripUpdates(tripUpdates);
    
    for (TripUpdateModel tripUpdate : updates) {
      String agencyId = getAgencyId(entitySource);

      addAgencyIdToTripId(tripUpdate, agencyId, entitySource);
      addAgencyIdToRouteId(tripUpdate, agencyId, entitySource);

      // Check stop id
      for (StopTimeUpdateModel stopTimeUpdate : tripUpdate.getStopTimeUpdates()) {
        addAgencyIdToStopId(stopTimeUpdate, agencyId, entitySource);
      }
      
      _persistor.persist(tripUpdate); 
    }
    
    return updates;
  }
  
  @Override
  public List<VehiclePositionModel> readVehiclePositions(
      FeedMessage vehiclePositions, GtfsRealtimeEntitySource entitySource) {
    _log.debug("reading VehiclePosition");
    List<VehiclePositionModel> models = GtfsRealtimeConversionLibrary.readVehiclePositions(vehiclePositions);
   
    for (VehiclePositionModel vehiclePosition : models) {
     
        String agencyId = getAgencyId(entitySource);
        
        addAgencyIdToTripId(vehiclePosition, agencyId, entitySource);
        addAgencyIdToRouteId(vehiclePosition, agencyId, entitySource);
        
        _persistor.persist(vehiclePosition);
      
    }
    return models;
  }
  
  @Override
  public List<AlertModel> readAlerts(FeedMessage alerts,
      GtfsRealtimeEntitySource entitySource) {
    List<AlertModel> updates = GtfsRealtimeConversionLibrary.readAlerts(alerts);
  
    for (AlertModel alert : updates) {
      
        // Update EntitySelectors to include the agency
        for (EntitySelectorModel esm : alert.getEntitySelectors()) {
      
          String agencyId = getAgencyId(entitySource);
          if (StringUtils.isEmpty(agencyId) && esm.getAgencyId() != null
              && esm.getAgencyId().length() > 0) {
            agencyId = esm.getAgencyId();
          }

          addAgencyIdToTripId(esm, agencyId, entitySource);
          addAgencyIdToRouteId(esm, agencyId, entitySource);
          addAgencyIdToStopId(esm, agencyId, entitySource);
        }
        
        _persistor.persist(alert);
      }
    
    return updates;
  }

  private String getAgencyId(GtfsRealtimeEntitySource entitySource) {
    String agencyId = "";
    // If there is only one agency for this feed, just use that
    if (entitySource.getAgencyIds().size() == 1) {
      agencyId = entitySource.getAgencyIds().get(0);
    }
    return agencyId;
  }
  
  private HasTripId addAgencyIdToTripId(HasTripId entity, String agencyId, GtfsRealtimeEntitySource entitySource) {

    if (StringUtils.isNotBlank(entity.getTripId())) {
      String tripId = entity.getTripId();
      if (agencyId.length() > 0) {
        entity.setTripId(new AgencyAndId(agencyId, tripId).toString());
      } else {
        // Look for the agency which has a match for this trip id
        TripEntry trip = entitySource.getTrip(entity.getTripId());
        if (trip == null) {
          _log.debug("No match found for trip: " + entity.getTripId());
        } else {
          entity.setTripId(trip.getId().toString());
          agencyId = trip.getId().getAgencyId();
        }
      }
    }

    
    return entity;
  }
  
  private HasRouteId addAgencyIdToRouteId(HasRouteId entity, String agencyId, GtfsRealtimeEntitySource entitySource) {
    if (StringUtils.isNotBlank(entity.getRouteId())) {
      String routeId = entity.getRouteId();
      if (agencyId.length() > 0) {
        entity.setRouteId(
            new AgencyAndId(agencyId, routeId).toString());
      } else { // Need to find agency for this routeId
        AgencyAndId id = entitySource.getRouteId(routeId);
        if (id == null) {
          _log.debug("No match found for route: " + routeId);
        } else {
          entity.setRouteId(id.toString());
          agencyId = id.getAgencyId();
        }
      }
    }
    
    return entity;
  }
  
  private HasStopId addAgencyIdToStopId(HasStopId entity, String agencyId, GtfsRealtimeEntitySource entitySource) {
    if (StringUtils.isNotBlank(entity.getStopId())) {
      String stopId = entity.getStopId();
      if (agencyId.length() > 0) {
        entity.setStopId(
            new AgencyAndId(agencyId, stopId).toString());
      } else { // Need to find agency for this stopId
        AgencyAndId id = entitySource.getStopId(stopId);
        if (id == null) {
          _log.debug(
              "No match found for entity selector stop: " + stopId);
        } else {
          entity.setStopId(id.toString());
        }
      }
    }
    return entity;
  }

}
