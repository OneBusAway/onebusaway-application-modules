/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.services.narrative;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

/**
 * Service methods supporting narrative querying. We define "narrative" as any
 * information that isn't used in raw schedule, trip planning, and routing
 * computations, but instead is simply there to provide human-readable labels to
 * results. For example, a stop's id is a raw data attribute that will be used
 * in method queries and data structures, but the stop name is a human-readable
 * element that typically isn't needed until constructing a result to display to
 * the user.
 * 
 * The narrative service has methods for querying narrative objects for various
 * low-level objects, such as {@link Agency}, {@link Stop},
 * {@link RouteCollectionEntry}, {@link Trip}, and {@link StopTime}.
 * 
 * @author bdferris
 * @see AgencyNarrative
 * @see StopNarrative
 * @see RouteCollectionNarrative
 * @see TripNarrative
 * @see StopTimeNarrative
 */
public interface NarrativeService {

  public AgencyNarrative getAgencyForId(String agencyId);

  public RouteCollectionNarrative getRouteCollectionForId(AgencyAndId routeCollectionId);

  public StopNarrative getStopForId(AgencyAndId stopId);

  public TripNarrative getTripForId(AgencyAndId tripId);

  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry);
  
  public ShapePoints getShapePointsForId(AgencyAndId id);
}
