package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
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
    StateData data = s0.getData();
    if (data.getNumBoardings() >= options.maxTransfers)
      return null;

    int transferTime = ItineraryWeightingLibrary.computeTransferTime(_transfer,
        options);
    double weight = ItineraryWeightingLibrary.computeTransferWeight(
        transferTime, options);

    State s1 = s0.incrementTimeInSeconds(transferTime + options.minTransferTime);

    /**
     * We're using options.boardCost as a transfer penalty
     */

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = ItineraryWeightingLibrary.computeTransferTime(_transfer,
        options);
    double weight = ItineraryWeightingLibrary.computeTransferWeight(
        transferTime, options);

    State s1 = s0.setTime(_instance.getBestArrivalTime());

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());
    return new TraverseResult(weight, s1, narrative);
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    DepartureVertex toVertex = new DepartureVertex(_context,
        _transfer.getStop(), time);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
