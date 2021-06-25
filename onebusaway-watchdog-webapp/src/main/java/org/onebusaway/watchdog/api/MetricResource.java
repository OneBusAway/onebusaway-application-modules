/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.watchdog.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.util.SystemTime;
import org.onebusaway.watchdog.model.Metric;
import org.onebusaway.watchdog.model.MetricConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MetricResource {

  protected static Logger _log = LoggerFactory.getLogger(MetricResource.class); 
  protected MetricConfiguration _configuration;
  protected ObjectMapper _mapper = new ObjectMapper();
  
  @Autowired
  public void setMetricConfiguration(MetricConfiguration mc) {
    _log.info("Setting MetricConfiguration: " + mc);
    _configuration = mc;
  }
  
  protected List<MonitoredDataSource> getDataSources() {
    return _configuration.getDataSources();
  }
  
  protected TransitDataService getTDS() {
    return _configuration.getTDS();
  }
  	  
  protected int getTotalRecordCount(String agencyId, String feedId) {
    int totalRecords = 0;

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      if (feedId == null || feedId.equals(mds.getFeedId())) {
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            totalRecords += result.getRecordsTotal();
          }
        }
      }
    }
    return totalRecords;
  }
  
  protected int getScheduledTrips(String agencyId) {
    return getScheduledTrips(agencyId, null);
  }
  
  protected int getScheduledTrips(String agencyId, String routeId) {
    Set<TripDetailsBean> agencyTrips = new HashSet<TripDetailsBean>();
      TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
      query.setAgencyId(agencyId);
      query.setMaxCount(Integer.MAX_VALUE);
      ListBean<TripDetailsBean> tripsForAgency = getTDS().getTripsForAgency(query);
      if (tripsForAgency == null) {
        return 0;
      }

      AgencyAndId routeAndId = new AgencyAndId(agencyId, routeId);
      for (TripDetailsBean trip : tripsForAgency.getList()) {
        // trip and tripId can be null for cancelled trips!
        if (trip != null && trip.getTripId() != null && trip.getTripId().startsWith(agencyId + "_")) {
          if (routeId == null || routeAndId.toString().equals(trip.getTrip().getRoute().getId())) {
            agencyTrips.add(trip);
          }
        }
      }
    _log.debug("scheduledTrips for (" + agencyId + ", " + routeId + "): " + agencyTrips.size() + " matched trips");
    return agencyTrips.size();
  }
  
  protected List<String> getValidRealtimeTripIds(String agencyId, String feedId) {
    Set<String> tripIds = new HashSet<String>();

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      if (feedId == null || feedId.equals(mds.getFeedId())) {
        for (String tripId : result.getMatchedTripIds()) {
          if (agencyId.equals(AgencyAndIdLibrary.convertFromString(tripId).getAgencyId())) {
            tripIds.add(tripId);
          }
        }
      }
    }
    List<String> prunedTripIds = new ArrayList<String>(tripIds.size());
    prunedTripIds.addAll(tripIds);
    return prunedTripIds;
  }

  protected int getUnmatchedTripIdCt(String agencyId, String feedId) {
    int unmatchedTripCt = 0;

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      if (feedId == null || feedId.equals(mds.getFeedId())) {
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            unmatchedTripCt += result.getUnmatchedTripIds().size();
          }
        }
      }
    } 
    return unmatchedTripCt;
  }
  
  protected int getMatchedStopCt(String agencyId, String feedId) {
    List<String> matchedStopIds = new ArrayList<String>();
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return 0;
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              matchedStopIds.addAll(result.getMatchedStopIds());
            }
          }
        }
      }
      return matchedStopIds.size();
    } catch (Exception e) {
      _log.error("getMatchedStopCt broke", e);
      return 0;
    }
  }
 
  protected int getUnmatchedStopCt(String agencyId, String feedId) {
    int unmatchedStops = 0;
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return 0;
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              unmatchedStops += result.getUnmatchedStopIds().size();
            }
          }
        }
      }
      return unmatchedStops;
    } catch (Exception e) {
      _log.error("getUnmatchedStopCt broke", e);
      return 0;
    }
  }
 
  protected int getLocationTotal(String agencyId, String feedId) {
    int locations = 0;
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return 0;
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              locations += result.getAllCoordinates().size();
            }
          }
        }
      }
      return locations;
    } catch (Exception e) {
      _log.error("getLocationTotal broke", e);
      return 0;
    }
  }
  
  protected int getInvalidLocation(String agencyId, String feedId) {
    int locations = 0;
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return 0;
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              locations += findInvalidLatLon(agencyId, result.getAllCoordinates()).size();
            }
          }
        }
      }
      return locations;
    } catch (Exception e) {
      _log.error("getInvalidLocation broke", e);
      return 0;
    }
  }
  
  protected String ok(String metricName, Object value) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setCurrentTimestamp(SystemTime.currentTimeMillis());
    metric.setMetricValue(value);
    metric.setResponse("SUCCESS");
    
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException e) {
      _log.error("metric serialization failed:" + e);
      return "{response=\"ERROR\"}";
    }
  }
  
  protected String error(String metricName, Exception e) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setErrorMessage(e.toString());
    metric.setResponse("ERROR");
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException ioe) {
      _log.error("metric serialization failed:" + ioe);
      return "{response=\"ERROR\"}";
    }
  }
 
  protected String error(String metricName, String errorMessage) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setErrorMessage(errorMessage);
    metric.setResponse("ERROR");
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException e) {
      _log.error("metric serialization failed:" + e);
      return "{response=\"ERROR\"}";
    }

  }
  
  private Collection<CoordinatePoint> findInvalidLatLon(String agencyId,
      Set<CoordinatePoint> coordinatePoints) {
    List<CoordinatePoint> invalid = new ArrayList<CoordinatePoint>();
    List<CoordinateBounds> bounds = getTDS().getAgencyIdsWithCoverageArea().get(agencyId);
    
    // ensure we have a valid bounding box for requested agency
    if (bounds == null || bounds.isEmpty()) {
      _log.warn("no bounds configured for agency " + agencyId);
      for (CoordinatePoint pt : coordinatePoints) {
        invalid.add(pt);
      }
      return invalid;
    }
    
    
    for (CoordinateBounds bound : bounds) {
      boolean found = false;
      for (CoordinatePoint pt : coordinatePoints) {
        // check if point is inside bounds
        if (bound.contains(pt)) {
          found = true;
        }
        if (!found) {
          invalid.add(pt);
        }
      }
    }
    _log.debug("agency " + agencyId + " had " + invalid.size() + " invalid out of " + coordinatePoints.size());
    return invalid;
  }

}