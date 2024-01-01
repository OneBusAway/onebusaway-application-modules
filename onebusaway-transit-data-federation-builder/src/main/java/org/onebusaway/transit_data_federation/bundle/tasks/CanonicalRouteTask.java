/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.util.Strings;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.RouteShape;
import org.onebusaway.gtfs.model.RouteStop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.CanonicalRoutesEntryImpl;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.CanonicalRoutesEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteShapeDirectionKey;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteStopCollectionEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * load optional route, stop, and shape data from GTFS that
 * represents idealized patterns of service, such as those shown
 * on a map / strip map.
 */
public class CanonicalRouteTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(CanonicalRouteTask.class);

  @Autowired
  private GtfsRelationalDao _gtfsDao;
  @Autowired
  private FederatedTransitDataBundle _bundle;

  private Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops = new HashMap<>();
  private Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape = new HashMap<>();
  private Map<AgencyAndId, Map<String, String>> routeIdToDirectionAndShape = new HashMap<>();

  @Override
  public void run() {

    indexRouteStops();
    indexRouteShapes();

    _log.info("found {} RouteStops and {} RouteShapes, flushing to bundle",
            routeIdToRouteStops.size(), routeShapeKeyToEncodedShape.size());
    // load into index
    CanonicalRoutesEntry canonicalRoutes = new CanonicalRoutesEntryImpl();
    canonicalRoutes.setRouteIdToRouteStops(routeIdToRouteStops);
    canonicalRoutes.setRouteShapeKeyToEncodedShape(routeShapeKeyToEncodedShape);
    canonicalRoutes.setRouteIdToDirectionAndShape(routeIdToDirectionAndShape);
    // flush to disk
    writeObject(canonicalRoutes);
  }

  private void writeObject(CanonicalRoutesEntry canonicalRoutes) {
    try {
      ObjectSerializationLibrary.writeObject(_bundle.getCanonicalRoutePath(), canonicalRoutes);
    } catch (IOException e) {
      _log.error("fatal exception building CanonicalRoutes:", e, e);
    }
  }

  private void indexRouteShapes() {
    for (RouteShape routeShape : _gtfsDao.getAllRouteShapes()) {
      RouteShapeDirectionKey rsde = new RouteShapeDirectionKey();
      try {
        char separator = AgencyAndId.ID_SEPARATOR;
        if (routeShape.getRouteId().indexOf(separator) == -1)
          separator = CanonicalRoutesEntry.ALT_ID_SEPARATOR;
        AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(routeShape.getRouteId(), separator);
        rsde.setRouteId(agencyAndId);
      } catch (IllegalStateException ise) {
        _log.error("illegal routeId " + routeShape.getRouteId());
        continue;
      }

      if (routeShape.getDirectionId() == null) {
        rsde.setDirectionId(Strings.EMPTY);
      } else {
        rsde.setDirectionId(routeShape.getDirectionId());
      }
      rsde.setType(routeShape.getType());
      // key: routeShapeDirectionType
      // value: encodedShape
      routeShapeKeyToEncodedShape.put(rsde, routeShape.getEncodedShape());


      if (!routeIdToDirectionAndShape.containsKey(routeShape.getRouteId())) {
        routeIdToDirectionAndShape.put(rsde.getRouteId(), new HashMap<>());
      }
      routeIdToDirectionAndShape.get(rsde.getRouteId()).put(nullSafeGet(routeShape.getDirectionId()), routeShape.getEncodedShape());
    }

  }

  private String nullSafeGet(String directionId) {
    if (directionId == null) return Strings.EMPTY;
    return directionId;
  }

  private void indexRouteStops() {
    Map<RouteAndDirectionId, RouteStopCollectionEntry> routeAndDirectionIdListMap = new HashedMap();

    // if gtfs files present and not empty
    for (RouteStop routeStop : _gtfsDao.getAllRouteStops()) {
      AgencyAndId routeId = null;
      // group by route + id before linked to just routeId
      try {
        routeId = AgencyAndIdLibrary.convertFromString(routeStop.getRouteId(), ':');
        if (!routeAndDirectionIdListMap.containsKey(createKey(routeId, routeStop.getDirectionId()))) {
          // this is the first one we've seen, create a placeholder for it
          RouteStopCollectionEntry rsce = new RouteStopCollectionEntry();
          // by convention we use the id and name of the first RouteStop
          rsce.setId(routeStop.getDirectionId());
          if (rsce.getName() == null) {
            if (StringUtils.isEmpty(routeStop.getName())) {
              rsce.setName(routeStop.getRouteId());
            } else {
              rsce.setName(routeStop.getName());
            }
          }
          rsce.setRouteId(routeId);

          routeAndDirectionIdListMap.put(createKey(routeId, routeStop.getDirectionId()), rsce);
        }
      } catch (IllegalStateException ise) {
        _log.error("illegal route " + routeStop.getRouteId() + " not in AgencyAndId format");
        continue;
      }

      RouteAndDirectionId key = createKey(routeId, routeStop.getDirectionId());
      RouteStopCollectionEntry entry = routeAndDirectionIdListMap.get(key);
      if (entry != null) {
        entry.add(routeStop);
      } else {
        _log.error("missing entry for route" + routeId + " and direction " + routeStop.getDirectionId());
      }
    }

    // now iterate over temporary index building up routeIdToRouteStops
    for (RouteAndDirectionId routeAndDirectionId : routeAndDirectionIdListMap.keySet()) {
      RouteStopCollectionEntry routeStopCollectionEntry = routeAndDirectionIdListMap.get(routeAndDirectionId);
      if (!routeIdToRouteStops.containsKey(routeAndDirectionId.getRouteId())) {
        routeIdToRouteStops.put(routeAndDirectionId.getRouteId(), new ArrayList<>());
      }
      if (routeStopCollectionEntry != null) {
        routeIdToRouteStops.get(routeAndDirectionId.getRouteId()).add(routeStopCollectionEntry);
      } else {
        _log.warn("null routeStopCollectionEntry for route " + routeAndDirectionId.getRouteId());
      }
    }

  }

  private RouteAndDirectionId createKey(AgencyAndId id1, String id2) {
    if (id2 == null)
      id2 = Strings.EMPTY;
    return new RouteAndDirectionId(id1, id2);
  }
  private static class RouteAndDirectionId {
    private AgencyAndId routeId;
    private String directionId;
    public RouteAndDirectionId(AgencyAndId routeId, String directionId) {
      this.routeId = routeId;
      this.directionId = directionId;
    }

    public AgencyAndId getRouteId() {
      return routeId;
    }

    public String getDirectionId() {
      return directionId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RouteAndDirectionId rd = (RouteAndDirectionId) o;
      return Objects.equals(routeId, rd.routeId)
              && Objects.equals(directionId, rd.directionId);
    }

    @Override
    public int hashCode() {
      int hashcode = 43;
      if (routeId != null) hashcode += routeId.hashCode();
      if (directionId != null) hashcode += directionId.hashCode();
      return hashcode;
    }
  }
}
