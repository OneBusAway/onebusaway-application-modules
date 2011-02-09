package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
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

  private final ArrivalAndDepartureInstance _from;

  private final ArrivalAndDepartureInstance _to;

  public BlockHopEdge(GraphContext context, ArrivalAndDepartureInstance from,
      ArrivalAndDepartureInstance to) {
    super(context);
    
    if( from == null)
      throw new IllegalArgumentException("from cannot be null");
    if( to == null)
      throw new IllegalArgumentException("to cannot be null");
      
    _from = from;
    _to = to;
  }

  @Override
  public TraverseResult traverse(State state0, TraverseOptions wo) {
    State state1 = state0.clone();
    int runningTime = computeRunningTime();
    state1.incrementTimeInSeconds(runningTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(runningTime, state1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State state0, TraverseOptions wo) {
    State state1 = state0.clone();
    int runningTime = computeRunningTime();
    state1.incrementTimeInSeconds(-runningTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(runningTime, state1, narrative);
  }

  /****
   * Private Methods
   ****/

  private int computeRunningTime() {
    long departure = _from.getBestDepartureTime();
    long arrival = _to.getBestArrivalTime();
    int runningTime = (int) ((arrival - departure) / 1000);
    return runningTime;
  }

  private EdgeNarrativeImpl createNarrative() {
    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = new BlockArrivalVertex(_context, _to);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
