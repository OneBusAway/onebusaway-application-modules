package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;

/**
 * In our transit-shed calculation, we effectively don't allow you to exit the
 * transit network once you've entered, avoiding the overhead of computing the
 * walk-shed from every transit stop you might arrive at. We care only that you
 * made it to the stop.
 * 
 * @author bdferris
 */
public class TransitShetVertexSkipStrategy implements VertexSkipStrategy {

  private static final long DEFAULT_MAX_TRIP_DURATION = 20 * 60 * 1000;

  private static final long DEFAULT_MAX_INITIAL_WAIT_TIME = 30 * 60 * 1000;

  @Override
  public boolean isVertexSkippedInFowardSearch(Vertex origin,
      State originState, SPTVertex vertex, TraverseOptions options) {

    /**
     * We aren't allowed to exit the transit network
     */
    if (vertex.mirror instanceof WalkFromStopVertex)
      return true;

    OTPConfiguration config = options.getExtension(OTPConfiguration.class);

    long maxTripDuration = DEFAULT_MAX_TRIP_DURATION;
    if (config != null && config.maxTripDuration != -1)
      maxTripDuration = config.maxTripDuration;

    long maxInitialWaitTime = DEFAULT_MAX_INITIAL_WAIT_TIME;
    if (config != null && config.maxInitialWaitTime != -1)
      maxInitialWaitTime = config.maxInitialWaitTime;

    State currentState = vertex.state;
    long tripDuration = currentState.time - originState.time;

    /*
    if (currentState.initialWaitTime > maxInitialWaitTime)
      return true;

    tripDuration -= currentState.initialWaitTime;
    */

    if (tripDuration > maxTripDuration)
      return true;

    return false;
  }

  @Override
  public boolean isVertexSkippedInReverseSearch(Vertex target,
      State targetState, SPTVertex vertex, TraverseOptions options) {
    return vertex.mirror instanceof WalkToStopState;
  }

}
