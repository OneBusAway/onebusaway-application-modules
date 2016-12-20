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

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.util.SystemTime;
import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/realtime/agency")
public class AgencyResource extends MetricResource {

  @Path("{agencyId}/last-update-delta")
  @GET
  @Produces("application/json")
  public Response getLastUpdateDelta(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      long lastUpdate = 0;
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("last-update-delta", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              lastUpdate += result.getLastUpdate();
            }
          }
        }
      }
      return Response.ok(ok("last-update-delta", (SystemTime.currentTimeMillis() - lastUpdate)/1000)).build();
    } catch (Exception e) {
      _log.error("getLastUpdateDelta broke", e);
      return Response.ok(error("last-update-delta", e)).build();
    }
  }

}
