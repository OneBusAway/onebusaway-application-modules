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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.watchdog.api.RESTEndpointsDocumenter.Endpoint;


@Path("/metric")
public class RootResource extends MetricResource {

  private static final String DEFAULT_API_PATH = "/onebusaway-watchdog-webapp/api";


  @Path("ping")
  @GET
  @Produces("application/json")
   public Response ping() {
    try {
    getTDS().getAgenciesWithCoverage();
    return Response.ok(ok("ping", 1)).build();
    } catch (Exception e) {
      _log.error("ping broke", e);
      return Response.ok(error("ping", e)).build();
    }
    
   }

  @Path("list-uris")
  @GET
  @Produces("application/json")
  public Response listUris() {
    try {
      RESTEndpointsDocumenter red = new RESTEndpointsDocumenter();
      List<Endpoint> endpoints = red.findRESTEndpoints("org.onebusaway.watchdog.api");
      StringBuffer json = new StringBuffer();
      json.append("[");
      for (Endpoint e : endpoints) {
        json.append("\"");
        json.append(e.uri);
        json.append("\"");
        json.append(",");
      }
      json.deleteCharAt(json.length()-1);
      json.append("]");
      
      return Response.ok(json.toString()).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }
  
  @Path("list-agencies")
  @GET
  @Produces("application/json")
  public Response listAgencies() {
    List<AgencyWithCoverageBean> agenciesWithCoverage = getTDS().getAgenciesWithCoverage();
    List<String> agencyIds = new ArrayList<String>();
    for (AgencyWithCoverageBean bean : agenciesWithCoverage) {
      agencyIds.add(bean.getAgency().getId());
    }
    Collections.sort(agencyIds);
    StringBuffer json = new StringBuffer();
    json.append("[");
    for (String agency : agencyIds) {
      json.append("\"");
      json.append(agency);
      json.append("\"");
      json.append(",");
    }
    json.deleteCharAt(json.length()-1);
    json.append("]");
    
    return Response.ok(json.toString()).build();
  }
}
