/**
 * Copyright (C) 2011 Google, Inc.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class AgencyEntryImpl implements AgencyEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private String _id;

  private List<StopEntry> _stops = Collections.emptyList();

  private List<RouteCollectionEntry> _routeCollections = Collections.emptyList();

  public void setId(String id) {
    _id = id;
  }

  public void setStops(List<StopEntry> stops) {
    _stops = stops;
  }

  public void setRouteCollections(List<RouteCollectionEntry> routeCollections) {
    _routeCollections = routeCollections;
  }

  /****
   * {@link AgencyEntry} Interface
   ****/

  @Override
  public String getId() {
    return _id;
  }

  @Override
  public List<StopEntry> getStops() {
    return _stops;
  }

  @Override
  public List<RouteCollectionEntry> getRouteCollections() {
    return _routeCollections;
  }
}
