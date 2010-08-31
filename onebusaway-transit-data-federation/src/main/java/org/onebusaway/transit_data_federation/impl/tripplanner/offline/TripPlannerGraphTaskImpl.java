package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks.BlockIdBlockOfTripsSourceImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks.BlockOfTripsSource;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks.TripIdBlockOfTripsSourceImpl;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.onebusaway.utility.ObjectSerializationLibrary;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

public class TripPlannerGraphTaskImpl implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(TripPlannerGraphImpl.class);

  @Autowired
  private ExtendedGtfsRelationalDao _gtfsDao;

  @Autowired
  private TransitDataFederationDao _whereDao;

  @Autowired
  private SessionFactory _sessionFactory;

  private FederatedTransitDataBundle _bundle;
  
  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  private Map<Object, Object> _uniques = new HashMap<Object, Object>();

  @Transactional
  public void run() {

    System.out.println("======== TripPlannerGraphFactory =>");

    DataLookup lookup = new DataLookup();
    TripPlannerGraphImpl graph = new TripPlannerGraphImpl();

    processStops(lookup, graph);

    List<BlockOfTripsSource> blockOfTripsSources = getBlockOfTripsSources();

    processBlockTrips(lookup, graph, blockOfTripsSources);
    sortStopTimeIndices(graph);

    try {
      ObjectSerializationLibrary.writeObject(_bundle.getTripPlannerGraphPath(), graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error writing graph to file", ex);
    }

    // Clear any cached memory
    _uniques.clear();
  }

  /**
   * Iterate over each stop, generating a StopEntry for the graph.
   * 
   * @param graph
   */
  private void processStops(DataLookup lookup, TripPlannerGraphImpl graph) {

    int stopIndex = 0;

    Collection<Stop> stops = _gtfsDao.getAllStops();

    for (Stop stop : stops) {

      if (stopIndex % 500 == 0)
        System.out.println("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      StopEntryImpl stopEntry = new StopEntryImpl(stop.getId(), stop.getLat(),
          stop.getLon(), new StopTimeIndexImpl());
      lookup.putStop(stopEntry);
      graph.putStopEntry(stopEntry);
    }
  }

  private List<BlockOfTripsSource> getBlockOfTripsSources() {

    List<BlockOfTripsSource> blockOfTripsSources = new ArrayList<BlockOfTripsSource>();

    Set<AgencyAndId> blockIds = new HashSet<AgencyAndId>();

    Collection<Route> routes = _gtfsDao.getAllRoutes();
    int routeIndex = 0;

    for (Route route : routes) {

      System.out.println("routes: " + (routeIndex++) + "/" + routes.size());

      List<Trip> trips = _gtfsDao.getTripsForRoute(route);
      for (Trip trip : trips) {
        if (trip.getBlockId() != null) {
          AgencyAndId blockId = new AgencyAndId(trip.getId().getAgencyId(),
              trip.getBlockId());
          blockIds.add(blockId);
        } else {
          blockOfTripsSources.add(new TripIdBlockOfTripsSourceImpl(trip.getId()));
        }
      }
    }

    for (AgencyAndId blockId : blockIds)
      blockOfTripsSources.add(new BlockIdBlockOfTripsSourceImpl(blockId));

    return blockOfTripsSources;
  }

  /**
   * We loop over blocks of trips, removing any trip that has no stop times,
   * sorting the remaining trips into the proper order, setting the 'nextTrip'
   * property for trips in the block, and setting the 'nextStop' property for
   * stops in the block.
   * 
   * @return
   */
  private void processBlockTrips(DataLookup lookup, TripPlannerGraphImpl graph,
      List<BlockOfTripsSource> blockOfTripsSources) {

    int blockIndex = 0;

    for (BlockOfTripsSource source : blockOfTripsSources) {

      if (blockIndex % 10 == 0)
        System.out.println("block: " + blockIndex + "/"
            + blockOfTripsSources.size());
      blockIndex++;

      List<Trip> trips = source.getTrips(_gtfsDao);
      List<StopTime> stopTimes = source.getStopTimes(_gtfsDao);

      checkBlockTrips(trips);

      Map<Trip, List<StopTime>> stopTimesByTrip = CollectionsLibrary.mapToValueList(
          stopTimes, "trip", Trip.class);

      List<Trip> tripsWithStopTimes = new ArrayList<Trip>();
      for (Trip trip : trips) {
        List<StopTime> stopTimesForTrip = stopTimesByTrip.get(trip);
        if (stopTimesForTrip != null && !stopTimesForTrip.isEmpty())
          tripsWithStopTimes.add(trip);
      }

      trips = tripsWithStopTimes;

      Collections.sort(trips, new BlockTripComparator(stopTimesByTrip));

      Map<Trip, Integer> tripOrder = new HashMap<Trip, Integer>();
      for (Trip trip : trips)
        tripOrder.put(trip, tripOrder.size());

      Collections.sort(stopTimes, new StopTimeComparator(tripOrder));

      int[] arrivalTimes = new int[stopTimes.size()];
      int[] departureTimes = new int[stopTimes.size()];

      interpolateArrivalAndDepartureTimes(stopTimes, arrivalTimes,
          departureTimes);

      Map<AgencyAndId, List<StopTimeEntryImpl>> stopTimesByTripId = getStopTimesByTripId(
          lookup, graph, stopTimes, arrivalTimes, departureTimes);

      processTrips(lookup, graph, trips, stopTimesByTripId);

      Trip prevTrip = null;
      TripEntryImpl prevEntry = null;
      StopTimeEntryImpl prevStopTime = null;

      TripEntryImpl firstTrip = null;

      for (Trip trip : trips) {

        TripEntryImpl tripEntry = lookup.getTrip(trip.getId());
        if (firstTrip == null)
          firstTrip = tripEntry;

        // Set PreviousTrip and NextTrip for the trips in the block
        if (prevTrip != null) {

          if (!prevEntry.getBlockId().equals(tripEntry.getBlockId()))
            throw new IllegalStateException(
                "expected trips to have same block id: prevTrip: id="
                    + prevEntry.getId() + " blockId=" + prevEntry.getBlockId()
                    + " nextTrip: id=" + tripEntry.getId() + " blockId="
                    + tripEntry.getBlockId());

          prevEntry.setNextTrip(tripEntry);
          tripEntry.setPrevTrip(prevEntry);
        }

        // Set the next and previous stops information
        for (StopTimeEntryImpl stopTime : stopTimesByTripId.get(trip.getId())) {

          if (prevStopTime != null) {

            int duration = stopTime.getArrivalTime()
                - prevStopTime.getDepartureTime();

            if (duration < 0) {
              throw new IllegalStateException();
            }

            StopEntryImpl fromStopEntry = prevStopTime.getStop();
            StopEntryImpl toStopEntry = stopTime.getStop();

            fromStopEntry.addNextStopWithMinTravelTime(toStopEntry, duration);
            toStopEntry.addPreviousStopWithMinTravelTime(fromStopEntry,
                duration);
          }
          prevStopTime = stopTime;
        }

        prevTrip = trip;
        prevEntry = tripEntry;
      }

      // Why?
      Session session = _sessionFactory.getCurrentSession();
      session.clear();
    }
  }

  private void checkBlockTrips(List<Trip> trips) {
    if (trips.isEmpty())
      return;

    Trip firstTrip = trips.get(0);

    for (Trip trip : trips) {
      if (!firstTrip.getId().getAgencyId().equals(trip.getId().getAgencyId()))
        throw new IllegalStateException(
            "trips in same block with different agency id: "
                + firstTrip.getId() + " vs " + trip.getId());
    }
  }

  private Map<AgencyAndId, List<StopTimeEntryImpl>> getStopTimesByTripId(
      DataLookup lookup, TripPlannerGraphImpl graph, List<StopTime> stopTimes,
      int[] arrivalTimes, int[] departureTimes) {

    Map<AgencyAndId, List<StopTimeEntryImpl>> stopTimesByTripId = new FactoryMap<AgencyAndId, List<StopTimeEntryImpl>>(
        new ArrayList<StopTimeEntryImpl>());

    int sequence = 0;

    for (int x = 0; x < stopTimes.size(); x++) {

      StopTime stopTime = stopTimes.get(x);

      Stop stop = stopTime.getStop();
      AgencyAndId aid = stop.getId();
      StopEntryImpl stopEntry = lookup.getStop(aid);
      StopTimeIndexImpl index = stopEntry.getStopTimes();

      AgencyAndId tripId = stopTime.getTrip().getId();

      // Reset the sequence to zero if we're on a new trip
      if (x > 0 && !tripId.equals(stopTimes.get(x - 1).getTrip().getId()))
        sequence = 0;

      int arrivalTime = arrivalTimes[x];
      int departureTime = departureTimes[x];

      StopTimeEntryImpl stopTimeEntry = new StopTimeEntryImpl();

      stopTimeEntry.setId(stopTime.getId());
      stopTimeEntry.setSequence(sequence);
      stopTimeEntry.setArrivalTime(arrivalTime);
      stopTimeEntry.setDepartureTime(departureTime);
      stopTimeEntry.setDropOffType(stopTime.getDropOffType());
      stopTimeEntry.setPickupType(stopTime.getPickupType());
      stopTimeEntry.setStop(stopEntry);
      stopTimeEntry.setShapeDistTraveled(stopTime.getShapeDistTraveled());

      LocalizedServiceId serviceId = getLocalizedServiceIdForStopTime(stopTime);
      serviceId = unique(serviceId);
      index.addStopTime(stopTimeEntry, serviceId);

      stopTimesByTripId.get(tripId).add(stopTimeEntry);

      sequence++;
    }

    return stopTimesByTripId;
  }

  private LocalizedServiceId getLocalizedServiceIdForStopTime(StopTime stopTime) {
    Trip trip = stopTime.getTrip();
    Route route = trip.getRoute();
    Agency agency = route.getAgency();
    TimeZone tz = TimeZone.getTimeZone(agency.getTimezone());
    return new LocalizedServiceId(trip.getServiceId(), tz);
  }

  /**
   * The {@link StopTime#getArrivalTime()} and
   * {@link StopTime#getDepartureTime()} properties are optional. This method
   * takes charge of interpolating the arrival and departure time for any
   * StopTime where they are missing. The interpolation is based on the distance
   * traveled along the current trip/block.
   * 
   * @param stopTimes
   * @param arrivalTimes
   * @param departureTimes
   */
  private void interpolateArrivalAndDepartureTimes(List<StopTime> stopTimes,
      int[] arrivalTimes, int[] departureTimes) {

    double[] distanceTraveled = getDistanceTraveledForStopTimes(stopTimes);

    SortedMap<Double, Integer> scheduleTimesByDistanceTraveled = new TreeMap<Double, Integer>();

    populateArrivalAndDepartureTimesByDistanceTravelledForStopTimes(stopTimes,
        distanceTraveled, scheduleTimesByDistanceTraveled);

    for (int i = 0; i < stopTimes.size(); i++) {

      StopTime stopTime = stopTimes.get(i);

      double d = distanceTraveled[i];

      boolean hasDeparture = stopTime.isDepartureTimeSet();
      boolean hasArrival = stopTime.isArrivalTimeSet();

      int departureTime = stopTime.getDepartureTime();
      int arrivalTime = stopTime.getArrivalTime();

      if (hasDeparture && !hasArrival) {
        arrivalTime = departureTime;
      } else if (hasArrival && !hasDeparture) {
        departureTime = arrivalTime;
      } else if (!hasArrival && !hasDeparture) {
        int t = departureTimes[i] = (int) InterpolationLibrary.interpolate(
            scheduleTimesByDistanceTraveled, d);
        arrivalTime = t;
        departureTime = t;
      }

      departureTimes[i] = departureTime;
      arrivalTimes[i] = arrivalTime;

      if (departureTimes[i] < arrivalTimes[i])
        throw new IllegalStateException();

      if (arrivalTime > departureTime)
        throw new IllegalStateException();

      if (i > 0 && arrivalTimes[i] < departureTimes[i - 1]) {

        StopTime prevStopTime = stopTimes.get(i - 1);
        Stop prevStop = prevStopTime.getStop();
        Stop stop = stopTime.getStop();

        if (prevStop.equals(stop)
            && arrivalTimes[i] == departureTimes[i - 1] - 1) {
          _log.info("fixing decreasing passingTimes: stopTimeA="
              + prevStopTime.getId() + " stopTimeB=" + stopTime.getId());
          arrivalTimes[i] = departureTimes[i - 1];
          if (departureTimes[i] < arrivalTimes[i])
            departureTimes[i] = arrivalTimes[i];
        } else {
          for (int x = 0; x < stopTimes.size(); x++) {
            StopTime st = stopTimes.get(x);
            System.err.println(x + " " + st.getId() + " " + arrivalTimes[x]
                + " " + departureTimes[x]);
          }
          throw new IllegalStateException();
        }
      }
    }
  }

  /**
   * Compute the distance traveled along the current trip/block for the sequence
   * of StopTimes. By default, we use {@link StopTime#getShapeDistTraveled()}
   * when present. However, when not present, we use the as-the-crow-flies
   * distance between each stop for each StopTime.
   * 
   * @param stopTimes
   * @return an array of distance traveled along the current trip/block for each
   *         StopTime
   */
  private double[] getDistanceTraveledForStopTimes(List<StopTime> stopTimes) {

    boolean shapeDistanceTraveledForAll = true;
    double[] distances = new double[stopTimes.size()];

    for (int i = 0; i < stopTimes.size(); i++) {
      StopTime stopTime = stopTimes.get(i);
      if (stopTime.getShapeDistTraveled() < 0)
        shapeDistanceTraveledForAll = false;
      else
        distances[i] = stopTime.getShapeDistTraveled();
    }

    if (!shapeDistanceTraveledForAll) {

      StopTime prev = null;
      double shapeDistanceTraveled = 0;

      for (int i = 0; i < stopTimes.size(); i++) {

        StopTime stopTime = stopTimes.get(i);

        if (prev != null) {
          Stop s1 = prev.getStop();
          Stop s2 = stopTime.getStop();
          if (s1 == null || s2 == null)
            throw new IllegalStateException("no stops for stopTimes: st1="
                + prev.getId() + " st2=" + stopTime.getId());
          shapeDistanceTraveled += DistanceLibrary.distance(s1, s2);
        }

        distances[i] = shapeDistanceTraveled;
        prev = stopTime;
        if (stopTime.getShapeDistTraveled() < 0)
          shapeDistanceTraveledForAll = false;
        else
          distances[i] = stopTime.getShapeDistTraveled();
      }
    }
    return distances;
  }

  /**
   * We have a list of StopTimes, along with their distance traveled along their
   * trip/block. For any StopTime that has either an arrival or a departure
   * time, we add it to the SortedMaps of arrival and departure times by
   * distance traveled.
   * 
   * @param stopTimes
   * @param distances
   * @param arrivalTimesByDistanceTraveled
   */
  private void populateArrivalAndDepartureTimesByDistanceTravelledForStopTimes(
      List<StopTime> stopTimes, double[] distances,
      SortedMap<Double, Integer> scheduleTimesByDistanceTraveled) {

    for (int i = 0; i < stopTimes.size(); i++) {

      StopTime stopTime = stopTimes.get(i);
      double d = distances[i];

      // We introduce distinct arrival and departure distances so that our
      // scheduleTimes map might have entries for arrival and departure times
      // that are not the same at a given stop
      double arrivalDistance = d;
      double departureDistance = d + 1e-6;

      /**
       * For StopTime's that have the same distance travelled, we keep the min
       * arrival time and max departure time
       */
      if (stopTime.getArrivalTime() >= 0) {
        if (!scheduleTimesByDistanceTraveled.containsKey(arrivalDistance)
            || scheduleTimesByDistanceTraveled.get(arrivalDistance) > stopTime.getArrivalTime())
          scheduleTimesByDistanceTraveled.put(arrivalDistance,
              stopTime.getArrivalTime());
      }

      if (stopTime.getDepartureTime() >= 0)
        if (!scheduleTimesByDistanceTraveled.containsKey(departureDistance)
            || scheduleTimesByDistanceTraveled.get(departureDistance) < stopTime.getDepartureTime())
          scheduleTimesByDistanceTraveled.put(departureDistance,
              stopTime.getDepartureTime());
    }
  }

  private void sortStopTimeIndices(TripPlannerGraphImpl graph) {
    for (StopEntryImpl entry : graph.getStops()) {
      StopTimeIndexImpl index = entry.getStopTimes();
      index.sort();
    }
  }

  /**
   * Loop over each trip, creating a TripEntry for the graph. Examine the
   * StopTimes for the trip, gathering statistics about minimum travel time
   * between consecutive stops
   * 
   * @param graph
   */
  private void processTrips(DataLookup lookup, TripPlannerGraphImpl graph,
      List<Trip> trips,
      Map<AgencyAndId, List<StopTimeEntryImpl>> stopTimesByTripId) {

    Set<AgencyAndId> blockIds = new HashSet<AgencyAndId>();
    Set<AgencyAndId> simulatedBlockIds = new HashSet<AgencyAndId>();

    for (Trip trip : trips) {

      RouteCollection routeCollection = _whereDao.getRouteCollectionForRoute(trip.getRoute());

      List<StopTimeEntryImpl> stopTimesForTrip = stopTimesByTripId.get(trip.getId());

      TripEntryImpl tripEntry = new TripEntryImpl();
      tripEntry.setId(trip.getId());
      tripEntry.setRouteId(unique(trip.getRoute().getId()));
      tripEntry.setRouteCollectionId(unique(routeCollection.getId()));

      AgencyAndId blockId = trip.getId();
      if (trip.getBlockId() != null) {
        blockId = new AgencyAndId(trip.getId().getAgencyId(), trip.getBlockId());
        blockIds.add(blockId);
      } else {
        simulatedBlockIds.add(blockId);
      }

      tripEntry.setBlockId(unique(blockId));
      tripEntry.setServiceId(unique(trip.getServiceId()));
      tripEntry.setStopTimes(new ArrayList<StopTimeEntry>(stopTimesForTrip));

      lookup.putTrip(trip.getId(), tripEntry);
      graph.putTripEntry(tripEntry);

      for (StopTimeEntryImpl stopTime : stopTimesForTrip)
        stopTime.setTrip(tripEntry);
    }

    blockIds.retainAll(simulatedBlockIds);

    if (!blockIds.isEmpty()) {
      _log.warn("blockId and simulated blockId overlap: " + blockIds);
      throw new IllegalStateException("blockId and simulated blockId overlap");
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T unique(T value) {
    T already = (T) _uniques.get(value);
    if (already == null) {
      _uniques.put(value, value);
      return value;
    }
    return already;
  }

  private static class StopTimeComparator implements Comparator<StopTime> {

    private Map<Trip, Integer> _tripOrder;

    public StopTimeComparator(Map<Trip, Integer> tripOrder) {
      _tripOrder = tripOrder;
    }

    public int compare(StopTime o1, StopTime o2) {

      Trip t1 = o1.getTrip();
      Trip t2 = o2.getTrip();

      int to1 = _tripOrder.get(t1);
      int to2 = _tripOrder.get(t2);

      int rc = to1 - to2;

      if (rc == 0)
        rc = o1.getStopSequence() - o2.getStopSequence();

      return rc;
    }
  }

  private static class BlockTripComparator implements Comparator<Trip> {

    private Map<Trip, List<StopTime>> _stopTimesByTrip;

    public BlockTripComparator(Map<Trip, List<StopTime>> stopTimesByTrip) {
      _stopTimesByTrip = stopTimesByTrip;
    }

    public int compare(Trip o1, Trip o2) {
      if ((o1.getBlockId() == null && o2.getBlockId() != null)
          || (o1.getBlockId() != null && !o1.getBlockId().equals(
              o2.getBlockId())))
        throw new IllegalStateException("blockId mismatch: " + o1 + " " + o2);

      int t1 = getAverageTime(o1);
      int t2 = getAverageTime(o2);

      return t1 - t2;
    }

    private int getAverageTime(Trip trip) {

      List<StopTime> stopTimes = _stopTimesByTrip.get(trip);

      if (stopTimes == null)
        throw new IllegalStateException("no StopTimes defined for trip " + trip);

      int departureTimes = 0;
      int departureTimeCounts = 0;

      for (StopTime stopTime : stopTimes) {
        if (stopTime.isDepartureTimeSet()) {
          departureTimes += stopTime.getDepartureTime();
          departureTimeCounts++;
        }
      }

      if (departureTimeCounts == 0)
        throw new IllegalStateException(
            "no StopTimes with departureTime for trip " + trip);

      return departureTimes / departureTimeCounts;
    }

  }

  private class DataLookup {

    Map<AgencyAndId, TripEntryImpl> _tripsById = new HashMap<AgencyAndId, TripEntryImpl>();

    Map<AgencyAndId, StopEntryImpl> _stopsById = new HashMap<AgencyAndId, StopEntryImpl>();

    public void putTrip(AgencyAndId tripId, TripEntryImpl entry) {
      _tripsById.put(tripId, entry);
    }

    public TripEntryImpl getTrip(AgencyAndId tripId) {
      return _tripsById.get(tripId);
    }

    public void putStop(StopEntryImpl stop) {
      _stopsById.put(stop.getId(), stop);
    }

    public StopEntryImpl getStop(AgencyAndId id) {
      return _stopsById.get(id);
    }
  }
}
