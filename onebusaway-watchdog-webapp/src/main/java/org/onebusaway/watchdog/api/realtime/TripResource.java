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
package org.onebusaway.watchdog.api.realtime;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/realtime/trip")
public class TripResource extends MetricResource {

  @Path("{agencyId}/total")
  @GET
  @Produces("application/json")
  public Response getTotalRecords(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int totalRecords = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("total-records", "no configured data sources")).build();
      }
      totalRecords = getTotalRecordCount(agencyId, feedId);
      return Response.ok(ok("total-records", totalRecords)).build();
    } catch (Exception e) {
      _log.error("getTotalRecords broke", e);
      return Response.ok(error("total-records", e)).build();
    }
  }
  
  @Path("{agencyId}/matched")
  @GET
  @Produces("application/json")
  public Response getMatchedTripCount(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("matched-trips", "con configured data sources")).build();
      }

      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      return Response.ok(ok("matched-trips", validRealtimeTrips)).build();
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips", e)).build();
    }
  }
  
  
  @Path("{agencyId}/unmatched")
  @GET
  @Produces("application/json")
  public Response getUnmatchedTrips(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int unmatchedTrips = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trips", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            _log.debug("examining agency=" + mAgencyId + " with unmatched trips=" + result.getUnmatchedTripIds().size());
            if (agencyId.equals(mAgencyId)) {
              unmatchedTrips += result.getUnmatchedTripIds().size();
            }
          }
        }
      }
      return Response.ok(ok("unmatched-trips", unmatchedTrips)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }

  @Path("{agencyId}/cancelled")
  @GET
  @Produces("application/json")
  public Response getCancelledTrips(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int cancelledTrips = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("cancelled-trips", "no configured data sources")).build();
      }
      cancelledTrips = getCancelledTripIdCt(agencyId, feedId);
      return Response.ok(ok("cancelled-trips", cancelledTrips)).build();
    } catch (Exception e) {
      _log.error("getcancelledTrips broke", e);
      return Response.ok(error("cancelled-trips", e)).build();
    }
  }

  @Path("{agencyId}/added")
  @GET
  @Produces("application/json")
  public Response getAddedTrips(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int addedTrips = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("added-trips", "no configured data sources")).build();
      }
      addedTrips = getAddedTripIdCt(agencyId, feedId);
      return Response.ok(ok("added-trips", addedTrips)).build();
    } catch (Exception e) {
      _log.error("getAddedTrips broke", e);
      return Response.ok(error("added-trips", e)).build();
    }
  }

  @Path("{agencyId}/unmatched-ids")
  @GET
  @Produces("application/json")
  public Response getUnmatchedTripIds(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<String> unmatchedTripIds = new ArrayList<String>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trip-ids", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              unmatchedTripIds.addAll(result.getUnmatchedTripIds());
            }
          }
        }
      }
      return Response.ok(ok("unmatched-trip-ids", unmatchedTripIds)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTripIds broke", e);
      return Response.ok(error("unmatched-trip-ids", e)).build();
    }
  }

  @Path("{agencyId}/cancelled-ids")
  @GET
  @Produces("application/json")
  public Response getCancelledTripIds(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<String> cancelledTripIds = new ArrayList<String>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("cancelled-trip-ids", "con configured data sources")).build();
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              cancelledTripIds.addAll(result.getCancelledTripIds());
            }
          }
        }
      }
      return Response.ok(ok("cancelled-trip-ids", cancelledTripIds)).build();
    } catch (Exception e) {
      _log.error("getCancelledTripIds broke", e);
      return Response.ok(error("cancelled-trip-ids", e)).build();
    }
  }

  @Path("{agencyId}/added-ids")
  @GET
  @Produces("application/json")
  public Response getAddedTripIds(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<String> addedTripIds = new ArrayList<String>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("added-trip-ids", "con configured data sources")).build();
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              addedTripIds.addAll(result.getAddedTripIds());
            }
          }
        }
      }
      return Response.ok(ok("added-trip-ids", addedTripIds)).build();
    } catch (Exception e) {
      _log.error("getAddedTripIds broke", e);
      return Response.ok(error("added-trip-ids", e)).build();
    }
  }

  @Path("{agencyId}/schedule-realtime-delta")
  @GET
  @Produces("application/json")
  public Response getScheduleRealtimeTripsDelta(@PathParam("agencyId") String agencyId, 
      @QueryParam("feedId") String feedId,
      @QueryParam("routeId") String routeId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("schedule-realtime-trips-delta", "con configured data sources")).build();
      }

      int scheduleTrips = getScheduledTrips(agencyId, routeId);
      int totalRecords = getTotalRecordCount(agencyId, feedId);
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      _log.debug("agencytrips size=" + scheduleTrips + ", validRealtimeTrips=" + validRealtimeTrips + ", totalRecords=" + totalRecords);
      int delta = scheduleTrips - validRealtimeTrips;
      return Response.ok(ok("schedule-realtime-trips-delta", delta)).build();
    } catch (Exception e) {
      _log.error("getScheduledRealtimeTripsDelta broke", e);
      return Response.ok(error("schedule-realtime-trips-delta", e)).build();
    }
  }


  @Path("{agencyId}/buses-in-service-percent")
  @GET
  @Produces("application/json")
  public Response getBusesInServicePercent(@PathParam("agencyId") String agencyId, 
       @QueryParam("feedId") String feedId,
       @QueryParam("routeId") String routeId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("buses-in-service-percent", "con configured data sources")).build();
      }

      double scheduleTrips = getScheduledTrips(agencyId, routeId);
      if (scheduleTrips < 1) {
        // prevent NaN -- late night service may not have scheduled trips
        // changed this to return 0 if no service -- makes more sense
        return Response.ok(ok("buses-in-service-percent", 0.)).build();
      }
      double validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      double cancelledTrips = getCancelledTripIdCt(agencyId, feedId);
      double addedTrips = getAddedTripIdCt(agencyId, feedId);

      _log.info("agencytrips size=" + scheduleTrips + ", validRealtimeTrips=" + validRealtimeTrips
              + " cancelled=" + cancelledTrips + ", addedTrips=" + addedTrips
              + " for feedId=" + feedId + ", routeId=" + routeId);
      // TODO: OBA doesn't support ADDED trips yet so not added to the metric!!!
      double percent = Math.abs(( (validRealtimeTrips /*+ addedTrips*/ - cancelledTrips) / scheduleTrips) * 100);
      return Response.ok(ok("buses-in-service-percent", percent)).build();
    } catch (Exception e) {
      _log.error("getBusesInServicePercent broke", e);
      return Response.ok(error("buses-in-service-percent", e)).build();
    }
  }
}
