package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

/**
 * A transit vehicle's journey between departure at one stop and arrival at the
 * next. This version represents a set of such journeys specified by a
 * TripPattern.
 */
public class BlockHopEdge extends AbstractEdge {

  private static final long serialVersionUID = 1L;

  private final long _serviceDate;

  private final BlockStopTimeEntry _from;

  private final BlockStopTimeEntry _to;

  public BlockHopEdge(GraphContext context, BlockStopTimeEntry from,
      BlockStopTimeEntry to, long serviceDate) {
    super(context);
    _serviceDate = serviceDate;
    _from = from;
    _to = to;
  }

  @Override
  public Vertex getFromVertex() {
    return new BlockDepartureVertex(_context, new StopTimeInstance(_from,
        _serviceDate));
  }

  @Override
  public Vertex getToVertex() {
    return new BlockArrivalVertex(_context, new StopTimeInstance(_to,
        _serviceDate));
  }

  @Override
  public TraverseResult traverse(State state0, TraverseOptions wo) {
    State state1 = state0.clone();
    int runningTime = _to.getStopTime().getArrivalTime()
        - _from.getStopTime().getDepartureTime();
    state1.incrementTimeInSeconds(runningTime);
    return new TraverseResult(runningTime, state1, this);
  }

  @Override
  public TraverseResult traverseBack(State state0, TraverseOptions wo) {
    State state1 = state0.clone();
    int runningTime = _to.getStopTime().getArrivalTime()
        - _from.getStopTime().getDepartureTime();
    state1.incrementTimeInSeconds(-runningTime);
    return new TraverseResult(runningTime, state1, this);
  }

  @Override
  public double getDistance() {
    return _to.getDistaceAlongBlock() - _from.getDistaceAlongBlock();
  }
}
