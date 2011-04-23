package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
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
public class TPOfflineBlockHopEdge extends AbstractEdge {

  private static final long serialVersionUID = 1L;

  private final StopTimeInstance _from;

  private final StopTimeInstance _to;

  public TPOfflineBlockHopEdge(GraphContext context, StopTimeInstance from,
      StopTimeInstance to) {
    super(context);

    if (from == null)
      throw new IllegalArgumentException("from cannot be null");
    if (to == null)
      throw new IllegalArgumentException("to cannot be null");

    _from = from;
    _to = to;
  }

  @Override
  public TraverseResult traverse(State state0, TraverseOptions wo) {

    int runningTime = computeRunningTime();
    State state1 = state0.incrementTimeInSeconds(runningTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(runningTime, state1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State state0, TraverseOptions wo) {

    int runningTime = computeRunningTime();
    State state1 = state0.incrementTimeInSeconds(-runningTime);

    EdgeNarrativeImpl narrative = createNarrative();

    return new TraverseResult(runningTime, state1, narrative);
  }

  /****
   * Private Methods
   ****/

  private int computeRunningTime() {
    long departure = _from.getDepartureTime();
    long arrival = _to.getArrivalTime();
    int runningTime = (int) ((arrival - departure) / 1000);
    return runningTime;
  }

  private EdgeNarrativeImpl createNarrative() {
    Vertex fromVertex = new TPOfflineBlockDepartureVertex(_context, _from);
    Vertex toVertex = new TPOfflineBlockArrivalVertex(_context, _to);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
