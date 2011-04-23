package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class TPOfflineBlockDwellEdge extends AbstractEdge {

  private final StopTimeInstance _instance;

  public TPOfflineBlockDwellEdge(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.incrementTimeInSeconds(dwellTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(dwellTime, state1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.incrementTimeInSeconds(-dwellTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(dwellTime, state1, narrative);
  }

  private EdgeNarrativeImpl createNarrative() {
    TPOfflineBlockArrivalVertex fromVertex = new TPOfflineBlockArrivalVertex(_context, _instance);
    TPOfflineBlockDepartureVertex toVertex = new TPOfflineBlockDepartureVertex(_context,
        _instance);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
