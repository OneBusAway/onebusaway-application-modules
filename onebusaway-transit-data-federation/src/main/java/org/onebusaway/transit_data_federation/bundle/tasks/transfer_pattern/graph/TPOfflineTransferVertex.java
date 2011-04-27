package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData.Editor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineTransferVertex extends AbstractTPOfflineBlockVertex {

  public TPOfflineTransferVertex(GraphContext context, StopTimeInstance instance) {
    super(context, instance);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new BoardEdge(_context));
  }

  @Override
  public String toString() {
    return "TPOfflineTransferVertex(" + _instance + ")";
  }

  private class BoardEdge extends AbstractEdge {

    public BoardEdge(GraphContext context) {
      super(context);
    }

    @Override
    public TraverseResult traverse(State s0, TraverseOptions options)
        throws NegativeWeightException {
      Editor s1 = s0.edit();
      s1.setEverBoarded(true);
      s1.incrementNumBoardings();
      Vertex toVertex = new TPOfflineBlockDepartureVertex(_context, _instance);
      EdgeNarrative narrative = new EdgeNarrativeImpl(
          TPOfflineTransferVertex.this, toVertex);
      return new TraverseResult(0, s1.createState(), narrative);
    }

    @Override
    public TraverseResult traverseBack(State s0, TraverseOptions options)
        throws NegativeWeightException {
      return null;
    }
  }
}
