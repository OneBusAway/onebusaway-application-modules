package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.impl.otp.TripSequenceShortestPathTree.ResultCollection;
import org.opentripplanner.routing.algorithm.strategies.SearchTerminationStrategy;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class SearchTerminationStrategyImpl implements SearchTerminationStrategy {

  @Override
  public boolean shouldSearchContinue(Vertex origin, Vertex target,
      SPTVertex current, ShortestPathTree spt, TraverseOptions traverseOptions) {

    OBATraverseOptions opts = (OBATraverseOptions) traverseOptions;

    Vertex currentVertex = current.mirror;

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

      SPTVertex minVertex = getMinVertex(byTripSequence.getVertices().values());

      if (current.weightSum > minVertex.weightSum * 1.5)
        return false;
    }

    return true;
  }

  private SPTVertex getMinVertex(Iterable<SPTVertex> vertices) {

    SPTVertex minVertex = null;
    double minWeight = Double.POSITIVE_INFINITY;

    for (SPTVertex vertex : vertices) {
      OBAStateData data = (OBAStateData) vertex.state.getData();
      if (data.isLookaheadItinerary())
        continue;
      if (vertex.weightSum < minWeight) {
        minVertex = vertex;
        minWeight = vertex.weightSum;
      }
    }

    return minVertex;
  }
}
