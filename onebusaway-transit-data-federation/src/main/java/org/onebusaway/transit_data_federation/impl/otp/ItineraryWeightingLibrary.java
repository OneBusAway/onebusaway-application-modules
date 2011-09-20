/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
