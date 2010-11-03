package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class BlockDwellEdge extends AbstractEdge {

  private final StopTimeInstance _instance;

  public BlockDwellEdge(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public Vertex getFromVertex() {
    return new BlockArrivalVertex(_context, _instance);
  }

  @Override
  public Vertex getToVertex() {
    return new BlockDepartureVertex(_context, _instance);
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.clone();
    state1.incrementTimeInSeconds(dwellTime);
    return new TraverseResult(dwellTime, state1, this);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BlockStopTimeEntry bst = _instance.getStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    State state1 = s0.clone();
    state1.incrementTimeInSeconds(-dwellTime);
    return new TraverseResult(dwellTime, state1, this);
  }

  @Override
  public double getDistance() {
    return 0;
  }
}
