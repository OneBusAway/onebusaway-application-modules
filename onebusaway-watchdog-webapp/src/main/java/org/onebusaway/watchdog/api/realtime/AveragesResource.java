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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.onebusaway.watchdog.api.LongTermAveragesResource;

@Path("/metric/realtime/delta")
public class AveragesResource extends LongTermAveragesResource {
  @Path("{agencyId}/average-matched-trips")
  @GET
  @Produces("application/json")
  public Response getLongTermDeltaMatched(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("long-term-delta-matched-trips", "no configured data sources")).build();
      }
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      int average = getAvgByAgency("matched-trips-average", agencyId);
      int longTermDelta =  validRealtimeTrips - average;
      _log.debug("current: " + validRealtimeTrips + ", average: " + average);
      return Response.ok(ok("long-term-delta-matched-trips", longTermDelta)).build();    
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips", e)).build();
    }
  }

  @Path("{agencyId}/average-matched-trips-pct")
  @GET
  @Produces("application/json")
  public Response getLongTermDeltaMatchedPct(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("long-term-delta-matched-trips-pct", "no configured data sources")).build();
      }
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      int average = getAvgByAgency("matched-trips-average", agencyId);
      int longTermDeltaPct =  average !=0 ? (int)(Math.round(((validRealtimeTrips - average)*100.0)/average)) : Integer.MAX_VALUE;
      _log.debug("current matched: " + validRealtimeTrips + ", average: " + average);
      return Response.ok(ok("long-term-delta-matched-trips-pct", longTermDeltaPct)).build();    
    } catch (Exception e) {
      _log.error("getMatchedTripCount broke", e);
      return Response.ok(error("matched-trips-pct", e)).build();
    }
  }

  @Path("{agencyId}/average-unmatched-trips")
  @GET
  @Produces("application/json")
  public Response getLongTermDeltaUnmatched(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      //int unmatchedTrips = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-trips", "no configured data sources")).build();
      }     
      int unmatchedTrips = getUnmatchedTripIdCt(agencyId, feedId);
      int average = getAvgByAgency("unmatched-trips-average", agencyId);
      int longTermDelta = unmatchedTrips - average;
      _log.debug("current: " + unmatchedTrips + ", average: " + average);
      return Response.ok(ok("long-term-delta-unmatched-trips", longTermDelta)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }
  
  @Path("{agencyId}/average-unmatched-trips-pct")
  @GET
  @Produces("application/json")
  public Response getLongTermDeltaUnmatchedPct(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int unmatchedTrips = getUnmatchedTripIdCt(agencyId, feedId);
      int average = getAvgByAgency("unmatched-trips-average", agencyId);
      int longTermDeltaPct = average != 0 ? (int)(Math.round(((unmatchedTrips - average) * 100.0)/average)) : Integer.MAX_VALUE;
      _log.debug("current unmatched: " + unmatchedTrips + ", average: " + average);
      return Response.ok(ok("unmatched-trips", longTermDeltaPct)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedTrips broke", e);
      return Response.ok(error("unmatched-trips", e)).build();
    }
  }
  @Path("{agencyId}/buses-in-service-pct")
  @GET
  @Produces("application/json")
  public Response getBusesInServicePct(@PathParam("agencyId") String agencyId, 
      @QueryParam("feedId") String feedId,
      @QueryParam("routeId") String routeId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("buses-in-service-pct", "no configured data sources")).build();
      }
      int scheduledTrips = getScheduledTrips(agencyId, routeId);
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      int percent = (int)Math.round((validRealtimeTrips * 100.0 / scheduledTrips));
      int average = getAvgByAgency("buses-in-service-pct", agencyId);
      int longTermDeltaPct =  average !=0 ? (int)Math.round(((percent - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("current pct in service: " + percent + ", average: " + average);
      return Response.ok(ok("long-term-delta-matched-trips-pct", longTermDeltaPct)).build();    
    } catch (Exception e) {
      _log.error("getBusesInServicePct broke", e);
      return Response.ok(error("buses-in-service-pct", e)).build();
    }
  }
  @Path("{agencyId}/matched-stops-pct")
  @GET
  @Produces("application/json")
  public Response getMatchedStops(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("matched-stops", "no configured data sources")).build();
      } 
      int matched = getMatchedStopCt(agencyId, feedId);
      int average = getAvgByAgency("matched-stops", agencyId);
      int percent = average !=0 ? (int)Math.round(((matched - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("current matched stops: " + matched + ", average: " + average);
      return Response.ok(ok("matched-stops-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getMatchedStops broke", e);
      return Response.ok(error("matched-stops-pct", e)).build();
    }
  }
  @Path("{agencyId}/unmatched-stops-pct")
  @GET
  @Produces("application/json")
  public Response getUnmatchedStops(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-stops", "no configured data sources")).build();
      } 
      int unmatched = getUnmatchedStopCt(agencyId, feedId);
      int average = getAvgByAgency("unmatched-stops", agencyId);
      int percent = average !=0 ? (int)Math.round(((unmatched - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("current unmatched stops: " + unmatched + ", average: " + average);
      return Response.ok(ok("unmatched-stops-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getUnmatchedStops broke", e);
      return Response.ok(error("unmatched-stops-pct", e)).build();
    }
  } 
  
  @Path("{agencyId}/trip-total-pct")
  @GET
  @Produces("application/json")
  public Response getTripTotalPct(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("trip-total-pct", "no configured data sources")).build();
      } 
      int total = getTotalRecordCount(agencyId, feedId);
      int average = getAvgByAgency("trip-total", agencyId);
      int percent = average !=0 ? (int)Math.round(((total - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("trip-total: " + total + ", average: " + average);
      return Response.ok(ok("trip-total-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getTripTotalPct broke", e);
      return Response.ok(error("trip-total-pct", e)).build();
    }
  }
  @Path("{agencyId}/trip-schedule-realtime-diff-pct")
  @GET
  @Produces("application/json")
  public Response getTripScheduleRealtimeDiff(@PathParam("agencyId") String agencyId, 
      @QueryParam("feedId") String feedId,
      @QueryParam("routeId") String routeId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("trip-schedule-realtime-diff-pct", "no configured data sources")).build();
      } 
      int scheduledTrips = getScheduledTrips(agencyId, routeId);
      int validRealtimeTrips = getValidRealtimeTripIds(agencyId, feedId).size();
      int diff = scheduledTrips - validRealtimeTrips;      
      int average = getAvgByAgency("trip-schedule-realtime-diff", agencyId);
      int percent = average !=0 ? (int)Math.round(((diff - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("trip-schedule-realtime-diff: " + diff + ", average: " + average);
      return Response.ok(ok("trip-schedule-realtime-diff-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getTripScheduleRealtimeDiff broke", e);
      return Response.ok(error("trip-schedule-realtime-diff-pct", e)).build();
    }
  }
  @Path("{agencyId}/location-total-pct")
  @GET
  @Produces("application/json")
  public Response getLocationTotalPct(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("location-total-pct", "no configured data sources")).build();
      } 
      int total = getLocationTotal(agencyId, feedId);
      int average = getAvgByAgency("location-total", agencyId);
      int percent = average !=0 ? (int)Math.round(((total - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("location-total: " + total + ", average: " + average);
      return Response.ok(ok("location-total-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getLocationTotal broke", e);
      return Response.ok(error("location-total-pct", e)).build();
    }
  }
  @Path("{agencyId}/location-invalid-lat-lon-pct")
  @GET
  @Produces("application/json")
  public Response getLocationInvalidPct(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("location-invalid-lat-lon-pct", "no configured data sources")).build();
      } 
      int total = getInvalidLocation(agencyId, feedId);
      int average = getAvgByAgency("location-invalid-lat-lon", agencyId);
      int percent = average !=0 ? (int)Math.round(((total - average)*100.0)/average) : Integer.MAX_VALUE;
      _log.debug("location-invalid-lat-lon: " + total + ", average: " + average);
      return Response.ok(ok("location-invalid-lat-lon-pct", percent)).build();    
    } catch (Exception e) {
      _log.error("getLocationInvalidPct broke", e);
      return Response.ok(error("location-invalid-lat-lon-pct", e)).build();
    }
  }
}
