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

import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

/**
 * In our transit-shed calculation, we effectively don't allow you to exit the
 * transit network once you've entered, avoiding the overhead of computing the
 * walk-shed from every transit stop you might arrive at. We care only that you
 * made it to the stop.
 * 
 * @author bdferris
 */
public class TransitShedVertexSkipStrategy implements VertexSkipStrategy {

  private static final long DEFAULT_MAX_TRIP_DURATION = 20 * 60 * 1000;

  private static final long DEFAULT_MAX_INITIAL_WAIT_TIME = 30 * 60 * 1000;

  @Override
  public boolean isVertexSkippedInFowardSearch(Vertex origin,
      State originState, State state, TraverseOptions options) {

    /**
     * We aren't allowed to exit the transit network
     */
    if (state.getVertex() instanceof WalkFromStopVertex)
      return true;

    OBATraverseOptions config = options.getExtension(OBATraverseOptions.class);

    long maxTripDuration = DEFAULT_MAX_TRIP_DURATION;
    if (config != null && config.maxTripDuration != -1)
      maxTripDuration = config.maxTripDuration;

    long maxInitialWaitTime = DEFAULT_MAX_INITIAL_WAIT_TIME;
    if (config != null && config.maxInitialWaitTime != -1)
      maxInitialWaitTime = config.maxInitialWaitTime;

    long tripDuration = state.getTime() - originState.getTime();

    OBAState obaState = (OBAState) state;
    long initialWaitTime = obaState.getInitialWaitTime();

    if (initialWaitTime > maxInitialWaitTime)
      return true;

    tripDuration -= initialWaitTime;

    if (tripDuration > maxTripDuration)
      return true;

    return false;
  }

  @Override
  public boolean isVertexSkippedInReverseSearch(Vertex target,
      State targetState, State state, TraverseOptions options) {
    return state.getVertex() instanceof WalkToStopState;
  }

}
