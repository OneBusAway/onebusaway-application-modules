package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class ArrivalAndTransferEdge extends AbstractEdge {

  private ArrivalAndDepartureInstance _instance;

  private StopTransfer _transfer;

  public ArrivalAndTransferEdge(GraphContext context,
      ArrivalAndDepartureInstance instance, StopTransfer transfer) {
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
    s1.incrementTimeInSeconds(transferTime + options.minTransferTime);

    /**
     * We're using options.boardCost as a transfer penalty
     */
    double weight = transferTime * options.walkReluctance + options.boardCost
        + options.minTransferTime * options.waitReluctance;

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(options);

    State s1 = s0.clone();
    s1.time = _instance.getBestArrivalTime();

    double weight = transferTime * options.walkReluctance + options.boardCost
        + options.minTransferTime * options.waitReluctance;

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    DepartureVertex toVertex = new DepartureVertex(_context, _transfer.getStop(), time);
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
