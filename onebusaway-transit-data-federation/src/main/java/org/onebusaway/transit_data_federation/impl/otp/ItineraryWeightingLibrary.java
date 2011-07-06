package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;

public class ItineraryWeightingLibrary {

  public static double computeWeightForWait(State state,
      int dwellTime) {

    TraverseOptions options = state.getOptions();
    double w = dwellTime * options.waitReluctance;

    /**
     * If this is the initial boarding, we penalize the wait time differently
     */
    if (state.getNumBoardings() == 0)
      w *= options.waitAtBeginningFactor;

    return w;
  }

  public static int computeTransferTime(StopTransfer transfer,
      TraverseOptions options) {

    int transferTime = transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    double walkingVelocity = options.speed;
    double distance = transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t;
  }

  public static double computeTransferWeight(int transferTime,
      TraverseOptions options) {
    return transferTime * options.walkReluctance + options.boardCost
        + options.minTransferTime * options.waitReluctance;
  }
}
