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
import javax.ws.rs.core.Response;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/realtime/trip")
public class TripResource extends MetricResource {

  @Path("/{agencyId}/total")
  @GET
  public Response getTotalRecords(@PathParam("agencyId") String agencyId) {
    try {
      int totalRecords = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
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
  
  @Path("/{agencyId}/matched")
  @GET
  public Response getMatchedTripCount(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
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
  
  
  @Path("/{agencyId}/unmatched")
  @GET
  public Response getUnmatchedTrips(@PathParam("agencyId") String agencyId) {
    try {
      int unmatchedTrips = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trips", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
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


  
  @Path("/{agencyId}/unmatched-ids")
  @GET
  public Response getUnmatchedTripIds(@PathParam("agencyId") String agencyId) {
    try {
      List<String> unmatchedTripIds = new ArrayList<String>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trip-ids", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
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

  @Path("/{agencyId}/schedule-realtime-delta")
  @GET
  public Response getScheduleRealtimeTripsDelta(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("schedule-realtime-trips-delta", "con configured data sources")).build();
      }

      int scheduleTrips = getScheduledTrips(agencyId);
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


  @Path("/{agencyId}/buses-in-service-percent")
  @GET
  public Response getBusesInServicePercent(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("buses-in-service-percent", "con configured data sources")).build();
      }

      double scheduleTrips = getScheduledTrips(agencyId);
      if (scheduleTrips < 1) {
        // prevent NaN -- late night service may not have scheduled trips
        return Response.ok(ok("buses-in-service-percent", 100.)).build();
      }
      double validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();

      _log.debug("agencytrips size=" + scheduleTrips + ", validRealtimeTrips=" + validRealtimeTrips);
      double percent = Math.abs((validRealtimeTrips / scheduleTrips) * 100);
      return Response.ok(ok("buses-in-service-percent", percent)).build();
    } catch (Exception e) {
      _log.error("getBusesInServicePercent broke", e);
      return Response.ok(error("buses-in-service-percent", e)).build();
    }
  }


}
