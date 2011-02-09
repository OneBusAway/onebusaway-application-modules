package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TransferAndDepartureEdge extends AbstractEdge {

  private ArrivalAndDepartureInstance _instance;

  private StopTransfer _transfer;

  public TransferAndDepartureEdge(GraphContext context,
      ArrivalAndDepartureInstance instance, StopTransfer transfer) {
    super(context);
    _instance = instance;
    _transfer = transfer;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(options);

    State s1 = s0.clone();
    s1.time = _instance.getBestArrivalTime();

    double weight = computeWeightForTransferTime(options, transferTime);

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Check if we've reached our transfer limit
     */
    if (s0.numBoardings >= options.maxTransfers)
      return null;

    int transferTime = computeTransferTime(options);

    State s1 = s0.clone();
    s1.incrementTimeInSeconds(-(transferTime + options.minTransferTime));

    double weight = computeWeightForTransferTime(options, transferTime);

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {
    Vertex fromVertex = new ArrivalVertex(_context, _transfer.getStop(), time);
    Vertex toVertex = new BlockDepartureVertex(_context, _instance);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }

  private double computeWeightForTransferTime(TraverseOptions options,
      int transferTime) {
    return transferTime * options.walkReluctance + options.boardCost
        + options.minTransferTime * options.waitReluctance;
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
