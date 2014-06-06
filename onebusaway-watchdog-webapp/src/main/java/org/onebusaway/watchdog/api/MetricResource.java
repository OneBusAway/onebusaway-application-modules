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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
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
    _configuration = mc;
  }
  

  protected List<MonitoredDataSource> getDataSources() {
    return _configuration.getDataSources();
  }
  
  protected TransitDataService getTDS() {
    return _configuration.getTDS();
  }
  
  protected int getTotalRecordCount(String agencyId) throws Exception {
    int totalRecords = 0;

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      for (String mAgencyId : result.getAgencyIds()) {
        if (agencyId.equals(mAgencyId)) {
          totalRecords += result.getRecordsTotal();
        }
      }
    }
    return totalRecords;
  }
  
  protected int getScheduledTrips(String agencyId) {
    Set<TripDetailsBean> agencyTrips = new HashSet<TripDetailsBean>();
    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    List<CoordinateBounds> allBounds = getTDS().getAgencyIdsWithCoverageArea().get(agencyId);
    
    for (CoordinateBounds bounds : allBounds) {
      query.setBounds(bounds);
      query.setTime(System.currentTimeMillis());
      query.setMaxCount(Integer.MAX_VALUE);
      
      TripDetailsInclusionBean inclusion = query.getInclusion();
      inclusion.setIncludeTripBean(true);
      ListBean<TripDetailsBean> allTrips =  getTDS().getTripsForBounds(query);
      if (allTrips == null) {
        continue;
      }
  
      _log.debug("allTrips size=" + allTrips.getList().size());
  
      for (TripDetailsBean trip : allTrips.getList()) {
        if (trip.getTripId().startsWith(agencyId + "_")) {
          agencyTrips.add(trip);
        }
      }
    }
    return agencyTrips.size();
  }
  
  protected List<String> getValidRealtimeTripIds(String agencyId) {
    Set<String> tripIds = new HashSet<String>();

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      for (String tripId : result.getMatchedTripIds()) {
        if (agencyId.equals(AgencyAndIdLibrary.convertFromString(tripId).getAgencyId())) {
          tripIds.add(tripId);
        }
      }
    }
    List<String> prunedTripIds = new ArrayList<String>(tripIds.size());
    prunedTripIds.addAll(tripIds);
    return prunedTripIds;
  }

  protected String ok(String metricName, Object value) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setCurrentTimestamp(System.currentTimeMillis());
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
  
}