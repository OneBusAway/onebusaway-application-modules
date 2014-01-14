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

import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceCollection;

/**
 * Service method for grouping a set of {@link StopSequence} objects into
 * {@link StopSequenceCollection} objects. Recall that a route typically has a
 * set of unique stop sequences visited by the various trips serving that route.
 * Here we group those stop sequences into collections, typically using
 * direction of travel as the main grouping strategy.
 * 
 * @author bdferris
 * 
 */
public interface StopSequenceCollectionService {

  /**
   * Group a set of stop sequence objects into a smaller set of stop sequence
   * collections, typically grouping the stop sequences by direction of travel
   * for the parent route.
   * 
   * @param sequences the set of stop sequences
   * @return the collection of stop sequence collection groups
   */
  public List<StopSequenceCollection> getStopSequencesAsCollections(
      List<StopSequence> sequences);
}