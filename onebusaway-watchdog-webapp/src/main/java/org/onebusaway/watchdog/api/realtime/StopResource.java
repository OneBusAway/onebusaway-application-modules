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

@Path("/metric/realtime/stop")
public class StopResource extends MetricResource {

  @Path("{agencyId}/matched")
  @GET
  @Produces("application/json")
  public Response getMatchedStopCount(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    List<String> matchedStopIds = new ArrayList<String>();
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("matched-stops", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              for (String stopId : result.getMatchedStopIds()) {
                matchedStopIds.add(stopId);
              }
            }
          }
        }                
      }
      
      return Response.ok(ok("matched-stops", matchedStopIds.size())).build();
    } catch (Exception e) {
      _log.error("getMatchedStopCount broke", e);
      return Response.ok(error("matched-stops", e)).build();
    }
  }

  @Path("{agencyId}/unmatched")
  @GET
  @Produces("application/json")
  public Response getUnmatchedStops(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      int unmatchedStops = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-stops", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            _log.debug("examining agency=" + mAgencyId + " with unmatched stops=" + result.getUnmatchedStopIds().size());
            if (agencyId.equals(mAgencyId)) {
              unmatchedStops += result.getUnmatchedStopIds().size();
            }
          }
        }
      }
      return Response.ok(ok("unmatched-stops", unmatchedStops)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedStops broke", e);
      return Response.ok(error("unmatched-stops", e)).build();
    }
  }

  @Path("{agencyId}/unmatched-ids")
  @GET
  @Produces("application/json")
  public Response getUnmatchedStopIds(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<String> unmatchedStopIds = new ArrayList<String>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("unmatched-stop-ids", "con configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              unmatchedStopIds.addAll(result.getUnmatchedStopIds());
            }
          }
        }
      }
      return Response.ok(ok("unmatched-stop-ids", unmatchedStopIds)).build();
    } catch (Exception e) {
      _log.error("getUnmatchedStopIds broke", e);
      return Response.ok(error("unmatched-stop-ids", e)).build();
    }
  }

}
