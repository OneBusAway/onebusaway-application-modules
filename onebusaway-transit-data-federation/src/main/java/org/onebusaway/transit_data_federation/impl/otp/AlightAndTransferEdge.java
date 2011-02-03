package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

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
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Check if we've reached our transfer limit
     */
    if (s0.numBoardings >= options.maxTransfers)
      return null;

    int transferTime = computeTransferTime(options);

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(transferTime);

    /**
     * We're using options.boardCost as a transfer penalty
     */
    double weight = transferTime * options.walkReluctance + options.boardCost;

    EdgeNarrativeImpl narrative = createNarrative();
    return new TraverseResult(weight, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(options);

    State s1 = s0.clone();
    s1.time = _instance.getArrivalTime();

    double weight = transferTime * options.walkReluctance + options.boardCost;

    EdgeNarrativeImpl narrative = createNarrative();
    return new TraverseResult(weight, s1, narrative);
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

  private int computeTransferTime(TraverseOptions options) {

    int transferTime = _transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    double walkingVelocity = options.speed;
    double distance = _transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t;
  }
}
