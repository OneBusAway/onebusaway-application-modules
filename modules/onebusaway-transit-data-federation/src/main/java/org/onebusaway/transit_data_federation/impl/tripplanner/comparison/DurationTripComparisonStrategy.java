/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.services.tripplanner.ETripComparison;

public class DurationTripComparisonStrategy implements TripComparisonStrategy {

  public ETripComparison compare(TripStateStats statsA, TripStateStats statsB) {

    assert statsA.getState().equals(statsB.getState());

    long durationA = statsA.getTripDuration();
    long durationB = statsB.getTripDuration();

    /**
     * Note that we used to return KEEP_BOTH if the trip durations were the
     * same, but now we don't. For the purposes of this comparison method (aka
     * finding the fastest path to a given stop) it doesn't matter how we got to
     * a given state, as long as it's the fastest. As such, if the two incoming
     * trips are exactly the same, we really only need to proceed with one of
     * them.
     */
    return durationA <= durationB ? ETripComparison.KEEP_A : ETripComparison.KEEP_B;
  }
}