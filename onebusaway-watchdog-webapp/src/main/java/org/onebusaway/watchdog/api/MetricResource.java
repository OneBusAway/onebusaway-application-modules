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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/metric")
public class MetricResource {

  private static Logger _log = LoggerFactory.getLogger(MetricResource.class);
  private TransitDataService _tds;
  private List<MonitoredDataSource> _dataSources = null;
  private ObjectMapper _mapper = new ObjectMapper();
  
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
    return Response.ok(ok("ping", 1)).build();
    } catch (Exception e) {
      _log.error("ping broke", e);
      return Response.ok(error("ping", e)).build();
    }
    
   }
  
  
  @Path("/agency-count")
  @GET
  public Response getAgencyCount() {
    try {
      int count = _tds.getAgenciesWithCoverage().size();
      return Response.ok(ok("agency-count", count)).build();
    } catch (Exception e) {
      _log.error("getAgencyCount broke", e);
      return Response.ok(error("agency-count", e)).build();
    }
  }
  
  @Path("/agency-id-list")
  @GET
  public Response getAgencyIdList() {
    try {
      
      List<AgencyWithCoverageBean> agencyBeans = _tds.getAgenciesWithCoverage();
      List<String> agencyIds = new ArrayList<String>();
      for (AgencyWithCoverageBean agency : agencyBeans) {
        agencyIds.add(agency.getAgency().getId());
      }
      return Response.ok(ok("agency-id-list", agencyIds)).build();
    } catch (Exception e) {
      _log.error("getAgencyIdList broke", e);
      return Response.ok(error("agency-id-list", e)).build();
    }
  }

  
  @Path("/realtime/{agencyId}/total-records")
  @GET
  public Response getTotalRecords(@PathParam("agencyId") String agencyId) {
    try {
      int totalRecords = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("total-records", "no configured data sources")).build();
      }
      totalRecords = getTotalRecordCount(agencyId);
      return Response.ok(ok("total-records", totalRecords)).build();
    } catch (Exception e) {
      _log.error("getTotalRecords broke", e);
      return Response.ok(error("total-records", e)).build();
    }
  }
  

  @Path("/realtime/{agencyId}/unmatched-trips")
  @GET
  public Response getUnmatchedTrips(@PathParam("agencyId") String agencyId) {
    try {
      int unmatchedTrips = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trips", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          _log.debug("examining agency=" + mAgencyId + " with unmatched trips=" + result.getUnmatchedTripIds().size());
          if (agencyId.equals(mAgencyId)) {
            unmatchedTrips += result.getUnmatchedTripIds().size();
          }
        }
      }
      return Response.ok(ok("unmatched-trips", unmatchedTrips)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }

  @Path("/realtime/{agencyId}/unmatched-stops")
  @GET
  public Response getUnmatchedStops(@PathParam("agencyId") String agencyId) {
    try {
      int unmatchedStops = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-stops", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          _log.debug("examining agency=" + mAgencyId + " with unmatched stops=" + result.getUnmatchedStopIds().size());
          if (agencyId.equals(mAgencyId)) {
            unmatchedStops += result.getUnmatchedStopIds().size();
          }
        }
      }
      return Response.ok(ok("unmatched-stops", unmatchedStops)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedStops broke", e);
      return Response.ok(error("unmatched-stops", e)).build();
    }
  }

  
  @Path("/realtime/{agencyId}/unmatched-trip-ids")
  @GET
  public Response getUnmatchedTripIds(@PathParam("agencyId") String agencyId) {
    try {
      List<String> unmatchedTripIds = new ArrayList<String>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trip-ids", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            unmatchedTripIds.addAll(result.getUnmatchedTripIds());
          }
        }
      }
      return Response.ok(ok("unmatched-trip-ids", unmatchedTripIds)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTripIds broke", e);
      return Response.ok(error("unmatched-trip-ids", e)).build();
    }
  }

  @Path("/realtime/{agencyId}/unmatched-stop-ids")
  @GET
  public Response getUnmatchedStopIds(@PathParam("agencyId") String agencyId) {
    try {
      List<String> unmatchedStopIds = new ArrayList<String>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-stop-ids", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            unmatchedStopIds.addAll(result.getUnmatchedStopIds());
          }
        }
      }
      return Response.ok(ok("unmatched-stop-ids", unmatchedStopIds)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedStopIds broke", e);
      return Response.ok(error("unmatched-stop-ids", e)).build();
    }
  }

  @Path("/realtime/{agencyId}/total-lat-lon-count")
  @GET
  public Response getTotalLatLonCount(@PathParam("agencyId") String agencyId) {
    try {
      List<CoordinatePoint> totalLatLons = new ArrayList<CoordinatePoint>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("total-lat-lon-count", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            totalLatLons.addAll(result.getAllCoordinates());
          }
        }
      }
      return Response.ok(ok("total-lat-lon-count", totalLatLons.size())).build();
    } catch (Exception e) {
      _log.error("getTotalLatLonCount broke", e);
      return Response.ok(error("total-lat-lon-count", e)).build();
    }
  }

  
  @Path("/realtime/{agencyId}/invalid-lat-lon-count")
  @GET
  public Response getInvalidLatLonCount(@PathParam("agencyId") String agencyId) {
    try {
      List<CoordinatePoint> invalidLatLons = new ArrayList<CoordinatePoint>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("invalid-lat-lon-count", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            invalidLatLons.addAll(findInvalidLatLon(agencyId, result.getAllCoordinates()));
          }
        }
      }
      return Response.ok(ok("invalid-lat-lon-count", invalidLatLons.size())).build();
    } catch (Exception e) {
      _log.error("getInvalidLatLonCount broke", e);
      return Response.ok(error("invalid-lat-lon-count", e)).build();
    }
  }


  @Path("/realtime/{agencyId}/invalid-lat-lons")
  @GET
  public Response getInvalidLatLons(@PathParam("agencyId") String agencyId) {
    try {
      List<CoordinatePoint> invalidLatLons = new ArrayList<CoordinatePoint>();
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("invalid-lat-lons", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            invalidLatLons.addAll(findInvalidLatLon(agencyId, result.getAllCoordinates()));
          }
        }
      }
      return Response.ok(ok("invalid-lat-lons", invalidLatLons)).build();
    } catch (Exception e) {
      _log.error("getInvalidLatLons broke", e);
      return Response.ok(error("invalid-lat-lons", e)).build();
    }
  }

  
  private Collection<CoordinatePoint> findInvalidLatLon(String agencyId,
      Set<CoordinatePoint> coordinatePoints) {
    List<CoordinatePoint> invalid = new ArrayList<CoordinatePoint>();
    List<CoordinateBounds> bounds = _tds.getAgencyIdsWithCoverageArea().get(agencyId);
    
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

  @Path("/realtime/{agencyId}/schedule-realtime-trips-delta")
  @GET
  public Response getScheduleRealtimeTripsDelta(@PathParam("agencyId") String agencyId,
      @QueryParam(value="lat") final Double lat,
      @QueryParam(value="lon") final Double lon,
      @QueryParam(value="latSpan") final Double latSpan,
      @QueryParam(value="lonSpan") final Double lonSpan) {
    try {
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("schedule-realtime-trips-delta", "con configured data sources")).build();
      }

      int scheduleTrips = getScheduledTrips(agencyId, lat, lon, latSpan, lonSpan);
      int totalRecords = getTotalRecordCount(agencyId);
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();
      _log.debug("agencytrips size=" + scheduleTrips + ", validRealtimeTrips=" + validRealtimeTrips + ", totalRecords=" + totalRecords);
      int delta = scheduleTrips - validRealtimeTrips;
      return Response.ok(ok("schedule-realtime-trips-delta", delta)).build();
    } catch (Exception e) {
      _log.error("getScheduledRealtimeTripsDelta broke", e);
      return Response.ok(error("schedule-realtime-trips-delta", e)).build();
    }
  }

  @Path("/realtime/{agencyId}/scheduled-trips")
  @GET
  public Response getScheduleTripCount(@PathParam("agencyId") String agencyId,
      @QueryParam(value="lat") final Double lat,
      @QueryParam(value="lon") final Double lon,
      @QueryParam(value="latSpan") final Double latSpan,
      @QueryParam(value="lonSpan") final Double lonSpan) {
    try {
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("scheduled-trips", "con configured data sources")).build();
      }

      int scheduleTrips = getScheduledTrips(agencyId, lat, lon, latSpan, lonSpan);
      
      return Response.ok(ok("scheduled-trips", scheduleTrips)).build();
    } catch (Exception e) {
      _log.error("getScheduleTripCount broke", e);
      return Response.ok(error("scheduled-trips", e)).build();
    }
  }
  
  @Path("/realtime/{agencyId}/matched-trips")
  @GET
  public Response getMatchedTripCount(@PathParam("agencyId") String agencyId) {
    try {
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("matched-trips", "con configured data sources")).build();
      }

      int validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();
      return Response.ok(ok("matched-trips", validRealtimeTrips)).build();
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips", e)).build();
    }
  }

  @Path("/realtime/{agencyId}/matched-stops")
  @GET
  public Response getMatchedStopCount(@PathParam("agencyId") String agencyId) {
    List<String> matchedStopIds = new ArrayList<String>();
    try {
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("matched-stops", "con configured data sources")).build();
      }

      for (MonitoredDataSource mds : _dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            matchedStopIds.addAll(result.getMatchedStopIds());
          }
        }
      }

      return Response.ok(ok("matched-stops", matchedStopIds.size())).build();
    } catch (Exception e) {
      _log.error("getMatchedStopCount broke", e);
      return Response.ok(error("matched-stops", e)).build();
    }
  }

  
  @Path("/realtime/{agencyId}/buses-in-service-percent")
  @GET
  public Response getBusesInServicePercent(@PathParam("agencyId") String agencyId,
      @QueryParam(value="lat") final Double lat,
      @QueryParam(value="lon") final Double lon,
      @QueryParam(value="latSpan") final Double latSpan,
      @QueryParam(value="lonSpan") final Double lonSpan) {
    try {
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("buses-in-service-percent", "con configured data sources")).build();
      }

      double scheduleTrips = getScheduledTrips(agencyId, lat, lon, latSpan, lonSpan);
      double validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();

      _log.debug("agencytrips size=" + scheduleTrips + ", validRealtimeTrips=" + validRealtimeTrips);
      double percent = Math.abs((validRealtimeTrips / scheduleTrips) * 100);
      return Response.ok(ok("buses-in-service-percent", percent)).build();
    } catch (Exception e) {
      _log.error("getBusesInServicePercent broke", e);
      return Response.ok(error("buses-in-service-percent", e)).build();
    }
  }


  @Path("/realtime/{agencyId}/last-update-delta")
  @GET
  public Response getLastUpdateDelta(@PathParam("agencyId") String agencyId) {
    try {
      long lastUpdate = 0;
      if (this._dataSources == null || this._dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("last-update-delta", "no configured data sources")).build();
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
      return Response.ok(ok("last-update-delta", (System.currentTimeMillis() - lastUpdate)/1000)).build();
    } catch (Exception e) {
      _log.error("getLastUpdateDelta broke", e);
      return Response.ok(error("last-update-delta", e)).build();
    }
  }
  
  private int getTotalRecordCount(String agencyId) throws Exception {
    int totalRecords = 0;

    for (MonitoredDataSource mds : _dataSources) {
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
  
  private int getScheduledTrips(String agencyId, Double lat, Double lon, Double latSpan, Double lonSpan) {
    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    if (lat == null || lon == null || latSpan == null || lonSpan == null) {
      _log.error("getScheduleTrips missing required coordinates:" 
    + "lat=" + lat
    + "latSpan=" + latSpan
    + "lon=" + lon
    + "lonSpan=" + lonSpan);
      return -1;
    }
    CoordinateBounds maxBounds = SphericalGeometryLibrary.boundsFromLatLonSpan(lat, lon, latSpan, lonSpan);
    query.setBounds(maxBounds);
    query.setTime(System.currentTimeMillis());
    query.setMaxCount(Integer.MAX_VALUE);
    
    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(true);
    _log.debug(lat + ", " + lon + " -> " + latSpan + ", " + lonSpan + "  :" + maxBounds.toString());
    ListBean<TripDetailsBean> allTrips =  _tds.getTripsForBounds(query);
    List<TripDetailsBean> agencyTrips = new ArrayList<TripDetailsBean>();
    if (allTrips == null) {
      return 0;
    }

    _log.debug("allTrips size=" + allTrips.getList().size());

    for (TripDetailsBean trip : allTrips.getList()) {
      if (trip.getTripId().startsWith(agencyId + "_")) {
        agencyTrips.add(trip);
      }
    }
    return agencyTrips.size();
  }
  
  private List<String> getValidRealtimeTripIds(String agencyId) {
    Set<String> tripIds = new HashSet<String>();

    for (MonitoredDataSource mds : _dataSources) {
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

  private String ok(String metricName, Object value) {
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
  
  private String error(String metricName, Exception e) {
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
 
  private String error(String metricName, String errorMessage) {
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