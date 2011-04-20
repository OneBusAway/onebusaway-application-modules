package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData.Editor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;

public class TPWalkFromStopToDestEdge extends AbstractEdge {

  private final TPState _pathState;

  public TPWalkFromStopToDestEdge(GraphContext context, TPState pathState) {
    super(context);
    _pathState = pathState;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    GraphPath walkToDest = _pathState.getWalkToDest();

    SPTVertex fromSptVertex = walkToDest.getFirstVertex();
    SPTVertex toSptVertex = walkToDest.getLastVertex();
    double w = toSptVertex.weightSum - fromSptVertex.weightSum;

    Editor s1 = s0.edit();
    s1.incrementWithStateDelta(fromSptVertex.state, toSptVertex.state);

    Vertex fromVertex = new TPPathVertex(_context, _pathState);
    Vertex toVertex = new TPDestinationVertex(_context,
        _pathState.getQueryData());
    EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(w, s1.createState(), narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    throw new UnsupportedOperationException();
  }
}
