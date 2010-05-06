/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.aggregation;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class NBestTripStrategy extends AbstractTripStrategy {

  private enum ETripCompareResult {
    KEEP_A, KEEP_B, KEEP_BOTH, NOT_COMPARABLE
  };

  private SortedSet<TimedTripEntry> _results = new TreeSet<TimedTripEntry>();

  private TimedTripEntry _firstTripToDestination = null;

  private TripPlannerConstraints _constraints;

  public NBestTripStrategy(TripPlannerConstants constants, TripPlannerConstraints constraints) {
    super(constants);
    _constraints = constraints;
  }

  public int getSize() {
    return _results.size();
  }

  public Collection<TripPlan> getTrips(Collection<TripPlan> results) {
    List<TripPlan> trips = new ArrayList<TripPlan>();
    for (TimedTripEntry entry : _results)
      trips.add(entry.getPlan());
    return trips;

  }

  /**
   * We want to keep track of the n best trips.
   * 
   * We consider two trips equivalent if they have the same sequence of trip
   * ids. That is to say, if two trips are basically the same (i.e. use the same
   * sequence of buses, but get off at slightly different stops), we want to
   * keep the shortest of the two. Trip equivalency is determined by the the
   * equality of keys retuned by {@link #getTripKeyForTrip(TripPlan)}.
   * 
   * We also track the n-fastest trips, for use in pruning subsequent trips in
   * the search space.
   * 
   * @param trip
   */
  public void addTrip(TripPlan trip) {

    double score = scoreTrip(trip);
    Set<AgencyAndId> tripIds = getTripIds(trip);
    TimedTripEntry newEntry = new TimedTripEntry(trip, score, tripIds);

    Set<TimedTripEntry> toPrune = new HashSet<TimedTripEntry>();

    if (_firstTripToDestination == null) {
      _firstTripToDestination = newEntry;
    } else if (trip.getTripEndTime() <= _firstTripToDestination.getPlan().getTripEndTime()) {
      if (trip.getTripEndTime() < _firstTripToDestination.getPlan().getTripEndTime()
          || score < _firstTripToDestination.getScore())
        _firstTripToDestination = newEntry;
    }

    for (TimedTripEntry existingEntry : _results) {
      ETripCompareResult result = compareTripPlans(existingEntry, newEntry);
      switch (result) {
        case KEEP_A:
          toPrune.add(newEntry);
          break;
        case KEEP_B:
          toPrune.add(existingEntry);
          break;
        case KEEP_BOTH:
          break;
        case NOT_COMPARABLE:
          break;
      }
    }

    _results.add(newEntry);
    _results.removeAll(toPrune);

    _results.add(_firstTripToDestination);

    if (_constraints.hasMaxTrips()) {
      while (_results.size() > _constraints.getMaxTrips())
        _results.remove(_results.last());
    }
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private ETripCompareResult compareTripPlans(TimedTripEntry entryA, TimedTripEntry entryB) {

    Set<AgencyAndId> tripIdsA = entryA.getTripIds();
    Set<AgencyAndId> tripIdsB = entryB.getTripIds();

    boolean haveCommonTripIds = false;
    boolean haveAllCommonTripIds = tripIdsA.equals(tripIdsB);

    for (AgencyAndId tripId : tripIdsB) {
      if (tripIdsA.contains(tripId))
        haveCommonTripIds = true;
    }

    if (haveAllCommonTripIds) {
      return entryA.getScore() == entryB.getScore() ? ETripCompareResult.KEEP_BOTH
          : (entryA.getScore() < entryB.getScore() ? ETripCompareResult.KEEP_A : ETripCompareResult.KEEP_B);
    } else if (haveCommonTripIds) {
      if (_constraints.hasMaxTripDurationRatio()) {
        double scoreA = entryA.getScore();
        double scoreB = entryB.getScore();
        if (scoreA < scoreB) {
          return scoreB > scoreA * _constraints.getMaxTripDurationRatio() ? ETripCompareResult.KEEP_A
              : ETripCompareResult.KEEP_BOTH;
        } else {
          return scoreA > scoreB * _constraints.getMaxTripDurationRatio() ? ETripCompareResult.KEEP_B
              : ETripCompareResult.KEEP_BOTH;
        }
      }
    }

    return ETripCompareResult.NOT_COMPARABLE;
  }

  private class TimedTripEntry implements Comparable<TimedTripEntry> {

    private TripPlan _plan;

    private double _score;

    private Set<AgencyAndId> _tripIds;

    public TimedTripEntry(TripPlan plan, double score, Set<AgencyAndId> tripIds) {
      _plan = plan;
      _score = score;
      _tripIds = tripIds;
    }

    public TripPlan getPlan() {
      return _plan;
    }

    public double getScore() {
      return _score;
    }

    public Set<AgencyAndId> getTripIds() {
      return _tripIds;
    }

    /**
     * Notice the greater-than sign in the comparison. We want entries with a
     * bigger score to float to the front of our priority queue so they will be
     * the first to be removed.
     */
    public int compareTo(TimedTripEntry o) {
      double a = _score;
      double b = o._score;
      return a == b ? 0 : (a < b ? -1 : 1);
    }
  }

}