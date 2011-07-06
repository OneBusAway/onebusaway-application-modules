package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;
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
    public State traverse(State s0) {

      TraverseOptions options = s0.getOptions();

      if (options.isArriveBy())
        return null;

      Vertex toVertex = new TPOfflineBlockDepartureVertex(_context, _instance);
      EdgeNarrative narrative = narrative(s0, TPOfflineTransferVertex.this,
          toVertex);

      StateEditor edit = s0.edit(this, narrative);
      edit.setEverBoarded(true);
      edit.incrementNumBoardings();
      return edit.makeState();
    }
  }
}
