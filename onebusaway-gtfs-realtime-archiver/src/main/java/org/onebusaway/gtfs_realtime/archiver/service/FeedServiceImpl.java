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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs_realtime.archiver.listener.GtfsRealtimeEntitySource;
import org.onebusaway.gtfs_realtime.archiver.model.AlertModel;
import org.onebusaway.gtfs_realtime.archiver.model.EntitySelectorModel;
import org.onebusaway.gtfs_realtime.archiver.model.StopTimeUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.model.TimeRangeModel;
import org.onebusaway.gtfs_realtime.archiver.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
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
  public List<TripUpdateModel> readTripUpdates(FeedMessage tripUpdates, GtfsRealtimeEntitySource entitySource) {
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
        
        // Prepend agency id to the trip id, route id, and stop id
        // If there is only one agency for this feed, just use that
        String agencyId = "";
        if (entitySource.getAgencyIds().size() == 1) {
          agencyId = entitySource.getAgencyIds().get(0);
        } else {
          // Look for the agency which has a match for this trip id
          TripEntry trip = entitySource.getTrip(tripUpdate.getTripId()); 
          if (trip == null) {   
            _log.debug("No match found for trip: " + tripUpdate.getTripId());
          } else {
            tripUpdate.setTripId(trip.getId().toString());
            agencyId = trip.getId().getAgencyId();           
          }
        }
        
        // Check route id
        if (tripUpdate.getRouteId() != null && tripUpdate.getRouteId().length() > 0) {
          String routeId = tripUpdate.getRouteId();
          if (agencyId.length() > 0) {
            tripUpdate.setRouteId(new AgencyAndId(agencyId, routeId).toString());
          } else {    // Need to find agency for this routeId
            AgencyAndId id = entitySource.getRouteId(routeId);
            if (id == null) {
              _log.debug("No match found for route: " + routeId);
            } else {
              tripUpdate.setRouteId(id.toString());
              agencyId = id.getAgencyId();
            }
          }
        }
        
        // Check stop id      
        for (StopTimeUpdateModel stopTimeUpdate : tripUpdate.getStopTimeUpdates()) {
          if (stopTimeUpdate.getStopId() != null && stopTimeUpdate.getStopId().length() > 0) {
            String stopId = stopTimeUpdate.getStopId();
            if (agencyId.length() > 0) {
              stopTimeUpdate.setStopId(new AgencyAndId(agencyId, stopId).toString());
            } else {    // Need to find agency for this stopId
              AgencyAndId id = entitySource.getStopId(stopId);
              if (id == null) {
                _log.debug("No match found for entity selector stop: " + stopId);
              } else {
                stopTimeUpdate.setStopId(id.toString());
                agencyId = id.getAgencyId();   
              }
            }
          }                  
        }   
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
      FeedMessage vehiclePositions, GtfsRealtimeEntitySource entitySource) {
    List<VehiclePositionModel> models = new ArrayList<VehiclePositionModel>();
    if (vehiclePositions == null) return models;
    long timestamp = vehiclePositions.getHeader().getTimestamp() * 1000;
    for (FeedEntity entity : vehiclePositions.getEntityList()) {
      VehiclePositionModel vehiclePosition = readVehiclePosition(entity, timestamp);
      if (vehiclePosition != null) {
        // Update VehiclePositionModel id fields to include agency id.
        String agencyId = "";
        if (entitySource.getAgencyIds().size() == 1) {
          agencyId = entitySource.getAgencyIds().get(0);
        }
        // Check for tripId
        if (vehiclePosition.getTripId() != null && !vehiclePosition.getTripId().isEmpty()) {
          String tripId = vehiclePosition.getTripId();
          if (agencyId.length() > 0) {
            vehiclePosition.setTripId(new AgencyAndId(agencyId, tripId).toString());
          } else {
            TripEntry trip = entitySource.getTrip(vehiclePosition.getTripId());          
            if (trip == null) {   
              _log.debug("No match found for vehicle position trip: " + vehiclePosition.getTripId());
              continue;   // If no match is found, throw it out.
            } else {
              vehiclePosition.setTripId(trip.getId().toString());
              agencyId = trip.getId().getAgencyId();
            }
          }
        }
        
        // Check for routeId
        if (vehiclePosition.getRouteId() != null && !vehiclePosition.getRouteId().isEmpty()) {
          String routeId = vehiclePosition.getRouteId();
          if (agencyId.length() > 0) {
            vehiclePosition.setRouteId(new AgencyAndId(agencyId, routeId).toString());
          } else {    // Need to find agency for this routeId
            AgencyAndId id = entitySource.getRouteId(routeId);
            if (id == null) {
              _log.debug("No match found for entity selector route: " + routeId);
            } else {
              vehiclePosition.setRouteId(id.toString());
              agencyId = id.getAgencyId();
            }
          }
        }
        
        // Check for stopId
        if (vehiclePosition.getStopId() != null && !vehiclePosition.getStopId().isEmpty()) {
          String stopId = vehiclePosition.getStopId();
          if (agencyId.length() > 0) {
            vehiclePosition.setStopId(new AgencyAndId(agencyId, stopId).toString());
          } else {    // Need to find agency for this stopId
            AgencyAndId id = entitySource.getStopId(stopId);
            if (id == null) {
              _log.debug("No match found for entity selector stop: " + stopId);
            } else {
              vehiclePosition.setStopId(id.toString());
              agencyId = id.getAgencyId();
            }
          }
        }
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
    if (entity.getVehicle().hasStopId()) {
      vpm.setStopId(entity.getVehicle().getStopId());
    }
    vpm.setTimestamp(new Date(timestamp));
    return vpm;
  }
  
  @Override
  public List<AlertModel> readAlerts(FeedMessage alerts, GtfsRealtimeEntitySource entitySource) {
    List<AlertModel> updates = new ArrayList<AlertModel>();
    if (alerts == null) {
      _log.error("nothing to do!");
      return updates;
    }
    List<FeedEntity> entityList = alerts.getEntityList();
    long timestamp = alerts.getHeader().getTimestamp() * 1000;
    for (FeedEntity entity : entityList) {
      AlertModel alert = readAlert(entity, timestamp);
      if (alert != null) {
        // Update EntitySelectors to include the agency  
        for (EntitySelectorModel esm : alert.getEntitySelectors()) {         
          String agencyId = "";
          if (entitySource.getAgencyIds().size() == 1) {
            agencyId = entitySource.getAgencyIds().get(0);
          } else if (esm.getAgencyId() != null && esm.getAgencyId().length() > 0) {
            agencyId = esm.getAgencyId();         
          } 
          
          // Check trip id
          if (esm.getTripId() != null && esm.getTripId().length() > 0) {
            String tripId = esm.getTripId();
            if (agencyId.length() > 0) {
              esm.setTripId(new AgencyAndId(agencyId, tripId).toString());
            } else {    // Need to find agency for this tripId
              TripEntry trip = entitySource.getTrip(tripId); 
              if (trip == null) {   
                _log.debug("No match found for entity selector trip: " + tripId);
              } else {
                esm.setTripId(trip.getId().toString());
                agencyId = trip.getId().getAgencyId();
              }
            }            
          }
          
          // Check route id          
          if (esm.getRouteId() != null && esm.getRouteId().length() > 0) {
            String routeId = esm.getRouteId();
            if (agencyId.length() > 0) {
              esm.setRouteId(new AgencyAndId(agencyId, routeId).toString());
            } else {    // Need to find agency for this routeId
              AgencyAndId id = entitySource.getRouteId(routeId);
              if (id == null) {
                _log.debug("No match found for entity selector route: " + routeId);
              } else {
                esm.setRouteId(id.toString());
                agencyId = id.getAgencyId();
              }
            }
          }
           
          // Check stop id
          if (esm.getStopId() != null && esm.getStopId().length() > 0) {
            String stopId = esm.getStopId();
            if (agencyId.length() > 0) {
              esm.setStopId(new AgencyAndId(agencyId, stopId).toString());
            } else {    // Need to find agency for this stopId
              AgencyAndId id = entitySource.getStopId(stopId);
              if (id == null) {
                _log.debug("No match found for entity selector stop: " + stopId);
              } else {
                esm.setStopId(id.toString());
                agencyId = id.getAgencyId();   
              }
            }
          }        
        }      
        _persistor.persist(alert);
        updates.add(alert);
      }
      
    }  
    return updates;
  }
  
  private AlertModel readAlert(FeedEntity entity, long timestamp) {
    if (entity.hasAlert()) {
      AlertModel alrt = new AlertModel();
      alrt.setTimestamp(new Date(timestamp));
      Alert alert = entity.getAlert();
      for (TimeRange tr : alert.getActivePeriodList()) {
        TimeRangeModel timeRange = readTimeRange(tr);
        if (timeRange != null) {
          timeRange.setAlert(alrt);
          alrt.addTimeRangeModel(timeRange);
        }
      }
      for (EntitySelector es : alert.getInformedEntityList()) {
        EntitySelectorModel entitySelector = readEntitySelector(es);
        if (entitySelector != null) {
          entitySelector.setAlert(alrt);
          alrt.addEntitySelectorModel(entitySelector);
        }
      }
      if (alert.hasCause()) {
        String cause = alert.getCause().getValueDescriptor().getFullName();
        cause = cause.substring(cause.lastIndexOf('.')+1);
        alrt.setCause(cause);
      }
      if (alert.hasEffect()) {
        String effect = alert.getEffect().getValueDescriptor().getFullName();
        effect = effect.substring(effect.lastIndexOf('.')+1);
        alrt.setEffect(effect);
      }
      if (alert.hasUrl()) {
        alrt.setUrl(alert.getUrl().getTranslation(0).getText());
      }
      if (alert.hasHeaderText()) {
        alrt.setHeaderText(alert.getHeaderText().getTranslation(0).getText());
      }
      if (alert.hasDescriptionText()) {
        alrt.setDescriptionText(alert.getDescriptionText().getTranslation(0).getText());
      }
      return alrt;
    }
    return null;
  }

  private TimeRangeModel readTimeRange(TimeRange tr) {
    if (tr == null) return null;
    TimeRangeModel trm = new TimeRangeModel();
    if (tr.hasStart()) {
      trm.setStart(tr.getStart());
    }
    if (tr.hasEnd()) {
      trm.setEnd(tr.getEnd());
    }
    return trm;
  }

  private EntitySelectorModel readEntitySelector(EntitySelector es) {
    if (es == null) return null;
    EntitySelectorModel esm = new EntitySelectorModel();
    if (es.hasAgencyId()) {
      esm.setAgencyId(es.getAgencyId());
    }
    if (es.hasRouteId()) {
      esm.setRouteId(es.getRouteId());
    }
    if (es.hasRouteType()) {
      esm.setRouteType(es.getRouteType());
    }
    if (es.hasStopId()) {
      esm.setStopId(es.getStopId());
    }
    if (es.hasTrip()) {
      TripDescriptor t = es.getTrip();
      if (t.hasTripId())
        esm.setTripId(t.getTripId());
      if (t.hasRouteId()) {
        esm.setRouteId(t.getRouteId());
      }
      if (t.hasStartTime()) {
        esm.setTripStartTime(t.getStartTime());
      }
      if (t.hasStartDate()) {
        esm.setTripStartDate(t.getStartDate());
      }
    }
    return esm;
  }

}
