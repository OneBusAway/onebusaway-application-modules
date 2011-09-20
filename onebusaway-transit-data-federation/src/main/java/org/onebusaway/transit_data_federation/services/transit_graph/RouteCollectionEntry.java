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
package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

/**
 * Why RouteCollection? Why not keep a 1-to-1 mapping between route short names
 * and {@link Route} entities? The issue is that some GTFS include multiple
 * {@link Route} entities with the same short name. These are often different
 * versions of the same route (local vs express).
 * 
 * @author bdferris
 * @see RouteEntry
 */
public interface RouteCollectionEntry {

  public AgencyAndId getId();

  public List<RouteEntry> getChildren();
}
