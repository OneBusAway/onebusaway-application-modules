package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;

public class ItineraryWeightingLibrary {

  public static double computeWeightForWait(TraverseOptions options, int dwellTime,
      State state) {

    double w = dwellTime * options.waitReluctance;

    /**
     * If this is the initial boarding, we penalize the wait time differently
     */
    StateData data = state.getData();
    if (data.getNumBoardings() == 0)
      w *= options.waitAtBeginningFactor;

    return w;
  }
}
