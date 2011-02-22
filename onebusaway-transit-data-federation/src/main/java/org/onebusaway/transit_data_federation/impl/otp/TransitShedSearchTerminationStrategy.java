package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;

public class TransitShedSearchTerminationStrategy implements
    SearchTerminationStrategy {

  private static final long DEFAULT_MAX_TRIP_DURATION = 20 * 60 * 1000;

  @Override
  public boolean terminateSearch(Vertex origin, State originState,
      SPTVertex vertex, TraverseOptions options) {

    long maxTripDuration = DEFAULT_MAX_TRIP_DURATION;
    OTPConfiguration config = options.getExtension(OTPConfiguration.class);
    if (config != null && config.maxTripDuration != -1)
      maxTripDuration = config.maxTripDuration;

    State currentState = vertex.state;
    long tripDuration = Math.abs(originState.time - currentState.time);

    if (tripDuration > maxTripDuration)
      return true;

    return false;
  }
}
