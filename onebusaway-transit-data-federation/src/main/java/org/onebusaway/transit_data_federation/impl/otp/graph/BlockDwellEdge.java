package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class BlockDwellEdge extends AbstractEdge {

  private final ArrivalAndDepartureInstance _instance;

  public BlockDwellEdge(GraphContext context, ArrivalAndDepartureInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.incrementTimeInSeconds(dwellTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(dwellTime, state1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.incrementTimeInSeconds(-dwellTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(dwellTime, state1, narrative);
  }

  private EdgeNarrativeImpl createNarrative() {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    BlockDepartureVertex toVertex = new BlockDepartureVertex(_context,
        _instance);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
