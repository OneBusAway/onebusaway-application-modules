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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.api.MetricResource;

@Path("/metric/realtime/location")
public class LocationResource extends MetricResource {

  @Path("{agencyId}/total")
  @GET
  @Produces("application/json")
  public Response getTotalLatLonCount(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<CoordinatePoint> totalLatLons = new ArrayList<CoordinatePoint>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("total-lat-lon-count", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              totalLatLons.addAll(result.getAllCoordinates());
            }
          }
        }
      }
      return Response.ok(ok("total-lat-lon-count", totalLatLons.size())).build();
    } catch (Exception e) {
      _log.error("getTotalLatLonCount broke", e);
      return Response.ok(error("total-lat-lon-count", e)).build();
    }
  }

  @Path("{agencyId}/invalid")
  @GET
  @Produces("application/json")
  public Response getInvalidLatLonCount(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<CoordinatePoint> invalidLatLons = new ArrayList<CoordinatePoint>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("invalid-lat-lon-count", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              invalidLatLons.addAll(findInvalidLatLon(agencyId, result.getAllCoordinates()));
            }
          }
        }
      }
      return Response.ok(ok("invalid-lat-lon-count", invalidLatLons.size())).build();
    } catch (Exception e) {
      _log.error("getInvalidLatLonCount broke", e);
      return Response.ok(error("invalid-lat-lon-count", e)).build();
    }
  }

  @Path("{agencyId}/invalid-lat-lons")
  @GET
  @Produces("application/json")
  public Response getInvalidLatLons(@PathParam("agencyId") String agencyId, @QueryParam("feedId") String feedId) {
    try {
      List<CoordinatePoint> invalidLatLons = new ArrayList<CoordinatePoint>();
      if (this.getDataSources() == null || this.getDataSources().isEmpty()) {
        _log.error("no configured data sources");
        return Response.ok(error("invalid-lat-lons", "no configured data sources")).build();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              invalidLatLons.addAll(findInvalidLatLon(agencyId, result.getAllCoordinates()));
            }
          }
        }
      }
      return Response.ok(ok("invalid-lat-lons", invalidLatLons)).build();
    } catch (Exception e) {
      _log.error("getInvalidLatLons broke", e);
      return Response.ok(error("invalid-lat-lons", e)).build();
    }
  }

  private Collection<CoordinatePoint> findInvalidLatLon(String agencyId,
      Set<CoordinatePoint> coordinatePoints) {
    List<CoordinatePoint> invalid = new ArrayList<CoordinatePoint>();
    List<CoordinateBounds> bounds = getTDS().getAgencyIdsWithCoverageArea().get(agencyId);
    
    // ensure we have a valid bounding box for requested agency
    if (bounds == null || bounds.isEmpty()) {
      _log.warn("no bounds configured for agency " + agencyId);
      for (CoordinatePoint pt : coordinatePoints) {
        invalid.add(pt);
      }
      return invalid;
    }
    
    
    for (CoordinateBounds bound : bounds) {
      boolean found = false;
      for (CoordinatePoint pt : coordinatePoints) {
        // check if point is inside bounds
        if (bound.contains(pt)) {
          found = true;
        }
        if (!found) {
          invalid.add(pt);
        }
      }
    }
    _log.debug("agency " + agencyId + " had " + invalid.size() + " invalid out of " + coordinatePoints.size());
    return invalid;
  }

}
