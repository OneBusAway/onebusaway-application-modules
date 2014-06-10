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
package org.onebusaway.watchdog.api.schedule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/schedule/trip")
public class TripResource extends MetricResource {

  @Path("/{agencyId}/total")
  @GET
  public Response getScheduleTripCount(@PathParam("agencyId") String agencyId) {
    try {
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("scheduled-trips", "con configured data sources")).build();
      }

      int scheduleTrips = getScheduledTrips(agencyId);
      
      return Response.ok(ok("scheduled-trips", scheduleTrips)).build();
    } catch (Exception e) {
      _log.error("getScheduleTripCount broke", e);
      return Response.ok(error("scheduled-trips", e)).build();
    }
  }
  

 
}
