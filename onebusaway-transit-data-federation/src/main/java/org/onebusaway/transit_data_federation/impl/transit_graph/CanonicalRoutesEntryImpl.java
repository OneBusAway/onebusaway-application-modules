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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.CanonicalRoutesEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteShapeDirectionKey;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteStopCollectionEntry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * implementation of bundle data structure supporting canonical route shapes and route stops.
 */
public class CanonicalRoutesEntryImpl implements CanonicalRoutesEntry, Serializable {

  private Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops = new HashMap<>();
  private Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape = new HashMap<>();
  private Map<AgencyAndId, Map<String, String>>routeIdToDirectionAndShape = new HashMap<>();

  @Override
  public void setRouteIdToRouteStops(Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops) {
    this.routeIdToRouteStops = routeIdToRouteStops;
  }

  @Override
  public List<RouteStopCollectionEntry> getRouteStopCollectionEntries(AgencyAndId routeId) {
    return routeIdToRouteStops.get(routeId);
  }

  @Override
  public void setRouteShapeKeyToEncodedShape(Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape) {
    this.routeShapeKeyToEncodedShape = routeShapeKeyToEncodedShape;
  }

  @Override
  public String getRouteEncodedShape(RouteShapeDirectionKey key) {
    return routeShapeKeyToEncodedShape.get(key);
  }

  @Override
  public Map<String, String> getDirectionToShapeMap(AgencyAndId routeId) {
    return routeIdToDirectionAndShape.get(routeId);
  }

  @Override
  public void setRouteIdToDirectionAndShape(Map<AgencyAndId, Map<String, String>> routeIdToDirectionAndShape) {
    this.routeIdToDirectionAndShape = routeIdToDirectionAndShape;
  }
}
