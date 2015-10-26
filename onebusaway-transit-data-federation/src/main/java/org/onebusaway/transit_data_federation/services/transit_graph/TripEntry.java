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
package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;

public interface TripEntry {

  public AgencyAndId getId();

  public RouteEntry getRoute();

  public RouteCollectionEntry getRouteCollection();

  public String getDirectionId();

  public BlockEntry getBlock();

  public LocalizedServiceId getServiceId();

  public AgencyAndId getShapeId();

  public List<StopTimeEntry> getStopTimes();

  /**
   * @return distance, in meters
   */
  public double getTotalTripDistance();

  /**
   * For trips that are operated with a fixed schedule, but marketed to riders
   * as headway-based service (by applying a {@link Frequency#getExactTimes()}
   * override value of 1 in their frequencies.txt GTFS), we attach the
   * {@link FrequencyEntry} associated with this trip.
   * 
   * @return
   */
  public FrequencyEntry getFrequencyLabel();
}
