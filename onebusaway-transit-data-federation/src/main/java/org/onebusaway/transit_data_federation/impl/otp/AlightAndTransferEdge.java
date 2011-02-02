package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerPreferences;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class AlightAndTransferEdge extends AbstractEdge {

  private StopTimeInstance _instance;

  private StopTransfer _transfer;

  public AlightAndTransferEdge(GraphContext context, StopTimeInstance instance,
      StopTransfer transfer) {
    super(context);
    _instance = instance;
    _transfer = transfer;
  }

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(_transfer);

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(transferTime);

    EdgeNarrativeImpl narrative = createNarrative();
    return new TraverseResult(transferTime, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(_transfer);

    State s1 = new State(_instance.getArrivalTime());

    EdgeNarrativeImpl narrative = createNarrative();
    return new TraverseResult(transferTime, s1, narrative);
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative() {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    WalkToStopVertex toVertex = new WalkToStopVertex(_context,
        _instance.getStop());
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }

  private int computeTransferTime(StopTransfer transfer) {

    int transferTime = transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    TripPlannerPreferences preferences = _context.getPreferences();
    double walkingVelocity = preferences.getWalkingVelocity();
    double distance = transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t + preferences.getMinTransferBufferTime();
  }
}
