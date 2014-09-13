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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/schedule/agency")
public class AgencyResource extends MetricResource {
  @Path("/total")
  @GET
  public Response getAgencyCount() {
    try {
      int count = getTDS().getAgenciesWithCoverage().size();
      return Response.ok(ok("agency-count", count)).build();
    } catch (Exception e) {
      _log.error("getAgencyCount broke", e);
      return Response.ok(error("agency-count", e)).build();
    }
  }
  
  @Path("/id-list")
  @GET
  public Response getAgencyIdList() {
    try {
      
      List<AgencyWithCoverageBean> agencyBeans = getTDS().getAgenciesWithCoverage();
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

}
