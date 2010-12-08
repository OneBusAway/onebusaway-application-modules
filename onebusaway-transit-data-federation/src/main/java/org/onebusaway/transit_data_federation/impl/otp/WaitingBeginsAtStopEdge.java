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
  public Vertex getToVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * We require a minimum amount of time between arriving at a stop and
     * actually boarding a bus
     */
    TripPlannerPreferences preferences = _context.getPreferences();
    int transferInterval = preferences.getMinTransferBufferTime();

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(transferInterval);

    TraverseResult result = new TraverseResult(transferInterval, s1, this);
    //result.setVertex(new WaitingAtStopVertex(_context, _stop, s1.getTime()));
    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    return new TraverseResult(0, s0, this);
  }

  @Override
  public double getDistance() {
    return 0;
  }

  @Override
  public String toString() {
    return "WaitingBeginsAtStopEdge(stop=" + _stop.getId() + ")";
  }
}
