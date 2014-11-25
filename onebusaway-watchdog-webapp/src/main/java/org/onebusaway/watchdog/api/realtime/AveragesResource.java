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

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.api.LongTermAveragesResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/metric/realtime/delta")
public class AveragesResource extends LongTermAveragesResource {
  @Path("/{agencyId}/average-matched-trips")
  @GET
  public Response getLongTermDeltaMatched(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("long-term-delta-matched-trips", "no configured data sources")).build();
      }
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();
      int average = getAvgByAgency("matched-trips-average", agencyId);
      int longTermDelta =  validRealtimeTrips - average;
      _log.info("current: " + validRealtimeTrips + ", average: " + average);
      return Response.ok(ok("long-term-delta-matched-trips", longTermDelta)).build();    
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips", e)).build();
    }
  }

  @Path("/{agencyId}/average-matched-trips-pct")
  @GET
  public Response getLongTermDeltaMatchedPct(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("long-term-delta-matched-trips-pct", "no configured data sources")).build();
      }
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId).size();
      int average = getAvgByAgency("matched-trips-average", agencyId);
      int longTermDeltaPct =  average !=0 ? ((validRealtimeTrips - average)*100)/average : 999999;
      _log.info("current: " + validRealtimeTrips + ", average: " + average);
      return Response.ok(ok("long-term-delta-matched-trips-pct", longTermDeltaPct)).build();    
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips-pct", e)).build();
    }
  }

  @Path("/{agencyId}/average-unmatched-trips")
  @GET
  public Response getLongTermDeltaUnmatched(@PathParam("agencyId") String agencyId) {
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
      int average = getAvgByAgency("unmatched-trips-average", "agencyId");
      int longTermDelta = unmatchedTrips - average;
      _log.info("current: " + unmatchedTrips + ", average: " + average);
      return Response.ok(ok("unmatched-trips", unmatchedTrips)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }
  
  @Path("/{agencyId}/average-unmatched-trips-pct")
  @GET
  public Response getLongTermDeltaUnmatchedPct(@PathParam("agencyId") String agencyId) {
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
      int average = getAvgByAgency("unmatched-trips-average", "agencyId");
      int longTermDeltaPct = average != 0 ? (unmatchedTrips - average)/average : 999999;
      _log.info("current: " + unmatchedTrips + ", average: " + average);
      return Response.ok(ok("unmatched-trips", unmatchedTrips)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }
}
