package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Map;

import org.opentripplanner.routing.algorithm.strategies.SearchTerminationStrategy;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class SearchTerminationStrategyImpl implements SearchTerminationStrategy {

  @Override
  public boolean shouldSearchContinue(Vertex origin, Vertex target,
      SPTVertex current, ShortestPathTree spt, TraverseOptions traverseOptions) {

    Vertex currentVertex = current.mirror;

    if (target == currentVertex) {

      return false;
      /*
      TripSequenceShortestPathTree tsSPT = (TripSequenceShortestPathTree) spt;
      Map<TripSequence, SPTVertex> byTripSequence = tsSPT.getVerticesByTripSequence(currentVertex);

      if (byTripSequence.isEmpty())
        throw new IllegalStateException("expected at least on result");

      if (byTripSequence.size() >= traverseOptions.numItineraries)
        return false;

      SPTVertex minVertex = getMinVertex(byTripSequence.values());

      if (current.weightSum > minVertex.weightSum * 1.5)
        return false;
      */
    }

    return true;
  }

  private SPTVertex getMinVertex(Iterable<SPTVertex> vertices) {

    SPTVertex minVertex = null;
    double minWeight = Double.POSITIVE_INFINITY;

    for (SPTVertex vertex : vertices) {
      if (vertex.weightSum < minWeight) {
        minVertex = vertex;
        minWeight = vertex.weightSum;
      }
    }

    return minVertex;
  }
}
