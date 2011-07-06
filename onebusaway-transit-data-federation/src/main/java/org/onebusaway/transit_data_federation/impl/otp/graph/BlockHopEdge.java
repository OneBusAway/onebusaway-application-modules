package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
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

    if (from == null)
      throw new IllegalArgumentException("from cannot be null");
    if (to == null)
      throw new IllegalArgumentException("to cannot be null");

    _from = from;
    _to = to;
  }

  @Override
  public State traverse(State s0) {

    EdgeNarrative narrative = createNarrative(s0);

    StateEditor edit = s0.edit(this, narrative);
    int runningTime = computeRunningTime();
    edit.incrementTimeInSeconds(runningTime);
    edit.incrementWeight(runningTime);

    return edit.makeState();
  }

  /****
   * Private Methods
   ****/

  private int computeRunningTime() {
    long departure = _from.getBestDepartureTime();
    long arrival = _to.getBestArrivalTime();
    return (int) ((arrival - departure) / 1000);
  }

  private EdgeNarrative createNarrative(State s0) {
    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = new BlockArrivalVertex(_context, _to);
    return narrative(s0, fromVertex, toVertex);
  }
}
