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
package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.impl.otp.TripSequenceShortestPathTree.ResultCollection;
import org.opentripplanner.routing.algorithm.strategies.SearchTerminationStrategy;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class SearchTerminationStrategyImpl implements SearchTerminationStrategy {

  @Override
  public boolean shouldSearchContinue(Vertex origin, Vertex target,
      State current, ShortestPathTree spt, TraverseOptions traverseOptions) {

    OBATraverseOptions opts = (OBATraverseOptions) traverseOptions;

    Vertex currentVertex = current.getVertex();

    if (target == currentVertex) {

      // return false;

      TripSequenceShortestPathTree tsSPT = (TripSequenceShortestPathTree) spt;
      ResultCollection byTripSequence = tsSPT.getVerticesByTripSequence(currentVertex);

      if (byTripSequence == null)
        throw new IllegalStateException("expected at least on result");

      if (byTripSequence.getItineraryCount() == 0)
        return true;

      if (byTripSequence.getItineraryCount() >= opts.numItineraries)
        return false;

      State minVertex = getMinVertex(byTripSequence.getStates().values());

      if (current.getWeight() > minVertex.getWeight() * 1.5)
        return false;
    }

    return true;
  }

  private State getMinVertex(Iterable<OBAState> states) {

    State minVertex = null;
    double minWeight = Double.POSITIVE_INFINITY;

    for (OBAState state : states) {
      if (state.isLookaheadItinerary())
        continue;
      if (state.getWeight() < minWeight) {
        minVertex = state;
        minWeight = state.getWeight();
      }
    }

    return minVertex;
  }
}
