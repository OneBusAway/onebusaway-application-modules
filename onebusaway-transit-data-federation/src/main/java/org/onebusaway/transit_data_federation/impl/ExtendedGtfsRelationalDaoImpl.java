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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.impl.SpringHibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;

public class ExtendedGtfsRelationalDaoImpl extends
    SpringHibernateGtfsRelationalDaoImpl implements ExtendedGtfsRelationalDao {

  @Override
  public Map<String, CoordinateBounds> getAgencyIdsAndBounds() {

    Map<String, CoordinateBounds> agencyIdsAndBounds = new HashMap<String, CoordinateBounds>();

    List<?> result = _ops.findByNamedQuery("latLonBoundsForStopsByAgencyId");
    for (Object row : result) {
      Object[] values = (Object[]) row;
      String agencyId = (String) values[0];
      Double minLat = (Double) values[1];
      Double minLon = (Double) values[2];
      Double maxLat = (Double) values[3];
      Double maxLon = (Double) values[4];
      agencyIdsAndBounds.put(agencyId, new CoordinateBounds(minLat, minLon,
          maxLat, maxLon));
    }
    return agencyIdsAndBounds;
  }

  @Override
  public List<AgencyAndId> getRouteIdsForAgencyId(String agencyId) {
    return _ops.findByNamedQueryAndNamedParam("routeIdsForAgencyId",
        "agencyId", agencyId);
  }

  @Override
  public List<AgencyAndId> getStopIdsForAgencyId(String agencyId) {
    return _ops.findByNamedQueryAndNamedParam("stopIdsForAgencyId", "agencyId",
        agencyId);
  }

  @Override
  public List<Route> getRoutesForStop(Stop stop) {
    return _ops.findByNamedQueryAndNamedParam("getRoutesForStop", "stop", stop);
  }

  @Override
  public List<StopTime> getStopTimesForBlockId(AgencyAndId blockId) {
    String[] names = {"agencyId", "blockId"};
    Object[] values = {blockId.getAgencyId(), blockId.getId()};
    return _ops.findByNamedQueryAndNamedParams("stopTimesByBlockId", names,
        values);
  }

  @Override
  public List<AgencyAndId> getShapePointIdsForRoutes(Collection<Route> routes) {

    if (routes.isEmpty())
      return new ArrayList<AgencyAndId>();

    List<AgencyAndId> shapePointIds = _ops.findByNamedQueryAndNamedParam(
        "shapePointIdsForRoutes", "routes", routes);

    List<AgencyAndId> validShapePointIds = new ArrayList<AgencyAndId>();
    for (AgencyAndId shapePointId : shapePointIds) {
      if (shapePointId != null && shapePointId.hasValues())
        validShapePointIds.add(shapePointId);
    }

    return validShapePointIds;
  }

}
