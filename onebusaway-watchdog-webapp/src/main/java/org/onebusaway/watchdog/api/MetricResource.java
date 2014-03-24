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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/metric")
public class MetricResource {

  private static Logger _log = LoggerFactory.getLogger(MetricResource.class);
  private TransitDataService _tds;
  private List<MonitoredDataSource> _dataSources = null;
  
  @Autowired
  public void setTransitDataService(TransitDataService tds) {
    _tds = tds;
  }
  
  @Autowired
  public void setMonitoredDataSources(List<MonitoredDataSource> dataSources) {
    _dataSources = dataSources;
  }
  
  @Path("/ping")
  @GET
   public Response ping() {
    try {
    _tds.getAgenciesWithCoverage();
    return Response.ok("1").build();
    } catch (Exception e) {
      _log.error("ping broke", e);
      return Response.serverError().build();
    }
    
   }
  
  
  @Path("/agency-count")
  @GET
  public Response getAgencyCount() {
    try {
      int count = _tds.getAgenciesWithCoverage().size();
      return Response.ok("" + count).build();
    } catch (Exception e) {
      _log.error("getAgencyCount broke", e);
      return Response.serverError().build();
    }
  }
  
  @Path("/realtime/{agencyId}/total-records")
  @GET
  public Response getTotalRecords(@PathParam("agencyId") String agencyId) {
    try {
      int totalRecords = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.serverError().build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            totalRecords += result.getRecordsTotal();
          }
        }
      }
      return Response.ok("" + totalRecords).build();
    } catch (Exception e) {
      _log.error("getTotalRecords broke", e);
      return Response.serverError().build();
    }
  }
  
  @Path("/realtime/{agencyId}/unmatched-trips")
  @GET
  public Response getUnmatchedTrips(@PathParam("agencyId") String agencyId) {
    try {
      int unmatchedTrips = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.serverError().build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            unmatchedTrips += result.getUnknownTripIds().size();
          }
        }
      }
      return Response.ok("" + unmatchedTrips).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.serverError().build();
    }
  }

  @Path("/realtime/{agencyId}/unmatched-trip-ids")
  @GET
  public Response getUnmatchedTripIds(@PathParam("agencyId") String agencyId) {
    try {
      List<String> unmatchedTripIds = new ArrayList<String>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.serverError().build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            unmatchedTripIds.addAll(result.getUnknownTripIds());
          }
        }
      }
      return Response.ok("" + unmatchedTripIds).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTripIds broke", e);
      return Response.serverError().build();
    }
  }

  
  @Path("/realtime/{agencyId}/last-update-delta")
  @GET
  public Response getLastUpdateDelta(@PathParam("agencyId") String agencyId) {
    try {
      long lastUpdate = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.serverError().build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            lastUpdate += result.getLastUpdate();
          }
        }
      }
      return Response.ok("" + (System.currentTimeMillis() - lastUpdate)/1000).build();
    } catch (Exception e) {
      _log.error("getLastUpdateDelta broke", e);
      return Response.serverError().build();
    }
  }
  
  
}