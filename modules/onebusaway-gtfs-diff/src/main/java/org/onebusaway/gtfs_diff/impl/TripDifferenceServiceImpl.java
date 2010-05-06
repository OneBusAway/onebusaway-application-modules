package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.model.MatchCollection;
import org.onebusaway.gtfs_diff.model.PotentialEntityMatch;
import org.onebusaway.gtfs_diff.model.ServiceId;
import org.onebusaway.gtfs_diff.services.GtfsDifferenceService;

import edu.washington.cs.rse.collections.same.ISameSet;
import edu.washington.cs.rse.collections.same.TreeUnionFind;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

public class TripDifferenceServiceImpl extends AbstractDifferenceServiceImpl
    implements GtfsDifferenceService {

  private static final double POTENTIAL_TRIP_MATCH_COMMON_STOPS_FACTOR = 5.0;

  private static final double POTENTIAL_TRIP_MATCH_SCHEDULE_DELTA_FACTOR = 1.0;

  private static final double POTENTIAL_TRIP_MATCH_MAX_SCORE = 90;

  @Transactional
  public void computeDifferences() {

    List<EntityMatch<Agency>> agencyMatches = getEntityMatches(_results,
        Agency.class);
    List<EntityMatch<Route>> routeMatches = getEntityMatches(agencyMatches,
        Route.class);

    for (EntityMatch<Route> routeMatch : routeMatches) {

      Route routeA = routeMatch.getEntityA();
      Route routeB = routeMatch.getEntityB();

      List<Trip> tripsA = _gtfsDao.getTripsForRoute(routeA);
      List<Trip> tripsB = _gtfsDao.getTripsForRoute(routeB);

      Map<AgencyAndId, Trip> tripsByIdA = mapAndTranlateIds(tripsA, "id",
          AgencyAndId.class, _results.getModelIdA());
      Map<AgencyAndId, Trip> tripsByIdB = mapAndTranlateIds(tripsB, "id",
          AgencyAndId.class, _results.getModelIdB());

      Set<AgencyAndId> commonIds = getCommonElements(tripsByIdA.keySet(),
          tripsByIdB.keySet());

      for (AgencyAndId id : commonIds) {
        Trip tripA = tripsByIdA.remove(id);
        Trip tripB = tripsByIdB.remove(id);
        EntityMatch<Trip> match = routeMatch.addMatch(new EntityMatch<Trip>(
            tripA, tripB));
        computeEntityPropertyDifferences(tripA, tripB, match, "id");
      }

      computePotentialMatches(tripsByIdA, tripsByIdB, routeMatch);

      for (Trip tripA : tripsByIdA.values())
        routeMatch.addMismatch(new EntityMismatch(tripA, null));

      for (Trip tripB : tripsByIdB.values())
        routeMatch.addMismatch(new EntityMismatch(null, tripB));
    }
  }

  private void computePotentialMatches(Map<AgencyAndId, Trip> tripsByIdA,
      Map<AgencyAndId, Trip> tripsByIdB, MatchCollection results) {

    ISameSet<AgencyAndId> serviceIdEquivalency = getServiceIdEquivalency();

    Map<Trip, List<StopTime>> stopTimesByTrip = getStopTimesByTrip(
        tripsByIdA.values(), tripsByIdB.values());

    List<PotentialEntityMatch<AgencyAndId>> potentialMatches = new ArrayList<PotentialEntityMatch<AgencyAndId>>();

    for (Map.Entry<AgencyAndId, Trip> entryA : tripsByIdA.entrySet()) {

      AgencyAndId tripIdA = entryA.getKey();
      Trip tripA = entryA.getValue();
      List<StopTime> stopTimesA = stopTimesByTrip.get(tripA);

      for (Map.Entry<AgencyAndId, Trip> entryB : tripsByIdB.entrySet()) {

        AgencyAndId tripIdB = entryB.getKey();
        Trip tripB = entryB.getValue();
        List<StopTime> stopTimesB = stopTimesByTrip.get(tripB);

        if (!serviceIdEquivalency.isSameSet(tripA.getServiceId(),
            tripB.getServiceId()))
          continue;

        int delta = compareFirstDeparture(stopTimesA, stopTimesB);
        if (delta > 60 * 60)
          continue;

        double score = computeDistance(stopTimesA, stopTimesB);
        potentialMatches.add(new PotentialEntityMatch<AgencyAndId>(tripIdA,
            tripIdB, score));
      }
    }

    Collections.sort(potentialMatches,
        new PotentialEntityMatchComparator<AgencyAndId>());

    for (PotentialEntityMatch<AgencyAndId> match : potentialMatches) {
      AgencyAndId tripIdA = match.getEntityA();
      AgencyAndId tripIdB = match.getEntityB();

      if (tripsByIdA.containsKey(tripIdA) && tripsByIdB.containsKey(tripIdB)) {
        Trip tripA = tripsByIdA.remove(tripIdA);
        Trip tripB = tripsByIdB.remove(tripIdB);
        if (match.getScore() < POTENTIAL_TRIP_MATCH_MAX_SCORE) {
          PotentialEntityMatch<Trip> m = new PotentialEntityMatch<Trip>(tripA,
              tripB, match.getScore());

          results.addMatch(m);
          computeEntityPropertyDifferences(tripA, tripB, m, "id");
        } else {
          System.out.println("wanted to add match: " + tripIdA + " " + tripIdB + " " + tripA.getRoute().getShortName() + " " + match.getScore());
        }
      }

      /*
      if (match.getScore() > POTENTIAL_TRIP_MATCH_MAX_SCORE)
        break;
      */
    }
  }

  private ISameSet<AgencyAndId> getServiceIdEquivalency() {

    TreeUnionFind<AgencyAndId> sameSet = new TreeUnionFind<AgencyAndId>();

    List<EntityMatch<ServiceId>> entityMatches = getEntityMatches(_results,
        ServiceId.class);

    for (EntityMatch<ServiceId> match : entityMatches) {
      ServiceId serviceIdA = match.getEntityA();
      ServiceId serviceIdB = match.getEntityB();
      sameSet.union(serviceIdA.getServiceId(), serviceIdB.getServiceId());
    }

    return sameSet;

  }

  private Map<Trip, List<StopTime>> getStopTimesByTrip(Collection<Trip> tripsA,
      Collection<Trip> tripsB) {
    Map<Trip, List<StopTime>> stopTimesByTrip = new HashMap<Trip, List<StopTime>>();
    for (Trip trip : tripsA)
      stopTimesByTrip.put(trip, _gtfsDao.getStopTimesForTrip(trip));
    for (Trip trip : tripsB)
      stopTimesByTrip.put(trip, _gtfsDao.getStopTimesForTrip(trip));
    return stopTimesByTrip;
  }

  private int compareFirstDeparture(List<StopTime> stopTimesA,
      List<StopTime> stopTimesB) {

    if (stopTimesA.isEmpty() || stopTimesB.isEmpty())
      return Integer.MAX_VALUE;

    StopTime stopTimeA = stopTimesA.get(0);
    StopTime stopTimeB = stopTimesB.get(0);

    return Math.abs(stopTimeA.getDepartureTime() - stopTimeB.getDepartureTime());
  }

  private double computeDistance(List<StopTime> stopTimesA,
      List<StopTime> stopTimesB) {

    Map<AgencyAndId, StopTime> stopTimesByStopA = mapAndTranlateIds(stopTimesA,
        "stop.id", AgencyAndId.class, _results.getModelIdA());
    Map<AgencyAndId, StopTime> stopTimesByStopB = mapAndTranlateIds(stopTimesB,
        "stop.id", AgencyAndId.class, _results.getModelIdB());

    Set<AgencyAndId> commonStops = getCommonElements(stopTimesByStopA.keySet(),
        stopTimesByStopB.keySet());

    double commonStopScore = (stopTimesA.size() + stopTimesB.size())
        - commonStops.size() * 2;

    double scheduleDelta = 0;

    for (AgencyAndId stop : commonStops) {
      StopTime stopTimeA = stopTimesByStopA.get(stop);
      StopTime stopTimeB = stopTimesByStopB.get(stop);
      double departureTimeDiff = Math.abs(stopTimeA.getDepartureTime()
          - stopTimeB.getDepartureTime());
      scheduleDelta += departureTimeDiff;
    }

    if (commonStops.size() > 0)
      scheduleDelta /= commonStops.size();

    return POTENTIAL_TRIP_MATCH_COMMON_STOPS_FACTOR * commonStopScore
        + POTENTIAL_TRIP_MATCH_SCHEDULE_DELTA_FACTOR * scheduleDelta;
  }

  private static class PotentialEntityMatchComparator<T> implements
      Comparator<PotentialEntityMatch<T>> {

    public int compare(PotentialEntityMatch<T> o1, PotentialEntityMatch<T> o2) {
      return Double.compare(o1.getScore(), o2.getScore());
    }
  }
}
