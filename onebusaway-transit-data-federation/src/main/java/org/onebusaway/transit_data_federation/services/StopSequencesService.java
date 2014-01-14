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
package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Service methods for generating {@link StopSequence} objects from a collection
 * of {@link Trip} objects and each trip's {@link StopTime} objects. Recall that
 * a stop sequence is a unique sequence of stops visited by a transit trip. So
 * typically, multiple trips will often refer to the same stop sequence, but not
 * always. Here we find all unique sequences for the set of trips.
 * 
 * @author bdferris
 * @see StopSequence
 */
public interface StopSequencesService {

  /**
   * Recall that a stop sequence is a unique sequence of stops visited by a
   * transit trip. So typically, multiple trips will often refer to the same
   * stop sequence, but not always. Here we find all unique sequences for the
   * set of trips.
   * 
   * @param trips set of trips
   * @return the set of unique stop sequences visited by the trips
   */
  public List<StopSequence> getStopSequencesForTrips(List<BlockTripEntry> trips);

}