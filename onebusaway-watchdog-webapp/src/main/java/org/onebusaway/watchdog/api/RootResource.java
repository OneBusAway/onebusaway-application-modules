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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/metric")
public class RootResource extends MetricResource {

  @Path("/ping")
  @GET
   public Response ping() {
    try {
    getTDS().getAgenciesWithCoverage();
    return Response.ok(ok("ping", 1)).build();
    } catch (Exception e) {
      _log.error("ping broke", e);
      return Response.ok(error("ping", e)).build();
    }
    
   }

  

}
