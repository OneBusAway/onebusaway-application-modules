package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerPreferences;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class WaitingBeginsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  public WaitingBeginsAtStopEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public Vertex getFromVertex() {
    return new WalkToStopVertex(_context, _stop);
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferInterval = getTransferInterval();

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(transferInterval);

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());

    TraverseResult result = new TraverseResult(transferInterval, s1, narrative);

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferInterval = getTransferInterval();

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(-transferInterval);

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());
    return new TraverseResult(0, s1, narrative);
  }

  @Override
  public String toString() {
    return "WaitingBeginsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  /****
   * Private Methods
   ****/

  /**
   * We require a minimum amount of time between arriving at a stop and actually
   * boarding a bus
   */
  private int getTransferInterval() {

    TripPlannerPreferences preferences = _context.getPreferences();
    int transferInterval = preferences.getMinTransferBufferTime();
    return transferInterval;
  }

  private EdgeNarrativeImpl createNarrative(long time) {

    WalkToStopVertex fromVertex = new WalkToStopVertex(_context, _stop);
    BoardVertex toVertex = new BoardVertex(_context, _stop, time);

    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
