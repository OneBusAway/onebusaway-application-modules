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
package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;
import java.util.Map;

/**
 * bundle data structure supporting canonical route shapes and route stops.
 */
public interface CanonicalRoutesEntry {
  public static char ALT_ID_SEPARATOR = ':';
  void setRouteIdToRouteStops(Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops);
  List<RouteStopCollectionEntry> getRouteStopCollectionEntries(AgencyAndId routeId);
  void setRouteShapeKeyToEncodedShape(Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape);
  String getRouteEncodedShape(RouteShapeDirectionKey key);
  Map<String, String> getDirectionToShapeMap(AgencyAndId routeId);
  void setRouteIdToDirectionAndShape(Map<AgencyAndId, Map<String, String>> routeIdToDirectionAndShape);
}
