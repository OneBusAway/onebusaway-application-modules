package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.utility.InterpolationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopTimeEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(StopTimeEntriesFactory.class);

  private DistanceAlongShapeLibrary _distanceAlongShapeLibrary;

  @Autowired
  public void setDistanceAlongShapeLibrary(
      DistanceAlongShapeLibrary distanceAlongShapeLibrary) {
    _distanceAlongShapeLibrary = distanceAlongShapeLibrary;
  }

  public List<StopTimeEntryImpl> processStopTimes(TransitGraphImpl graph,
      List<StopTime> stopTimes, TripEntryImpl tripEntry, ShapePoints shapePoints) {

    // In case the list is unmodifiable
    stopTimes = new ArrayList<StopTime>(stopTimes);

    Collections.sort(stopTimes, new StopTimeComparator());

    List<StopTimeEntryImpl> stopTimeEntries = createInitialStopTimeEntries(
        graph, stopTimes);

    for (StopTimeEntryImpl stopTime : stopTimeEntries)
      stopTime.setTrip(tripEntry);

    ensureStopTimesHaveShapeDistanceTraveledSet(stopTimeEntries, shapePoints);
    ensureStopTimesHaveTimesSet(stopTimes, stopTimeEntries);

    return stopTimeEntries;
  }

  private List<StopTimeEntryImpl> createInitialStopTimeEntries(
      TransitGraphImpl graph, List<StopTime> stopTimes) {

    List<StopTimeEntryImpl> stopTimeEntries = new ArrayList<StopTimeEntryImpl>();
    int sequence = 0;

    for (StopTime stopTime : stopTimes) {

      Stop stop = stopTime.getStop();
      AgencyAndId stopId = stop.getId();
      StopEntryImpl stopEntry = graph.getStopEntryForId(stopId);

      StopTimeEntryImpl stopTimeEntry = new StopTimeEntryImpl();

      stopTimeEntry.setId(stopTime.getId());
      stopTimeEntry.setSequence(sequence);
      stopTimeEntry.setDropOffType(stopTime.getDropOffType());
      stopTimeEntry.setPickupType(stopTime.getPickupType());
      stopTimeEntry.setStop(stopEntry);

      stopTimeEntries.add(stopTimeEntry);
      sequence++;
    }

    return stopTimeEntries;
  }

  /**
   * We have to make sure shape distance traveled is set, even if we don't have
   * shape information
   * 
   * @param stopTimes
   * @param shapePoints potentially null
   */
  private void ensureStopTimesHaveShapeDistanceTraveledSet(
      List<StopTimeEntryImpl> stopTimes, ShapePoints shapePoints) {

    // Do we have shape information?
    if (shapePoints != null) {

      PointAndIndex[] stopTimePoints = _distanceAlongShapeLibrary.getDistancesAlongShape(
          shapePoints, stopTimes);
      for (int i = 0; i < stopTimePoints.length; i++) {
        PointAndIndex pindex = stopTimePoints[i];
        StopTimeEntryImpl stopTime = stopTimes.get(i);
        stopTime.setShapePointIndex(pindex.index);
        stopTime.setShapeDistTraveled(pindex.distanceAlongShape);
      }

    } else {

      // Make do without
      double d = 0;
      StopTimeEntryImpl prev = null;
      for (StopTimeEntryImpl stopTime : stopTimes) {
        stopTime.setShapeDistTraveled(d);
        if (prev != null) {
          CoordinatePoint from = prev.getStop().getStopLocation();
          CoordinatePoint to = stopTime.getStop().getStopLocation();
          d += SphericalGeometryLibrary.distance(from, to);
        }
        prev = stopTime;
      }
    }
  }

  private void ensureStopTimesHaveTimesSet(List<StopTime> stopTimes,
      List<StopTimeEntryImpl> stopTimeEntries) {

    double[] distanceTraveled = getDistanceTraveledForStopTimes(stopTimeEntries);

    int[] arrivalTimes = new int[stopTimes.size()];
    int[] departureTimes = new int[stopTimes.size()];

    interpolateArrivalAndDepartureTimes(stopTimes, distanceTraveled,
        arrivalTimes, departureTimes);

    int sequence = 0;
    int accumulatedSlackTime = 0;
    StopTimeEntryImpl prevStopTimeEntry = null;

    for (StopTimeEntryImpl stopTimeEntry : stopTimeEntries) {

      int arrivalTime = arrivalTimes[sequence];
      int departureTime = departureTimes[sequence];

      stopTimeEntry.setArrivalTime(arrivalTime);
      stopTimeEntry.setDepartureTime(departureTime);

      stopTimeEntry.setAccumulatedSlackTime(accumulatedSlackTime);
      accumulatedSlackTime += stopTimeEntry.getDepartureTime()
          - stopTimeEntry.getArrivalTime();

      if (prevStopTimeEntry != null) {

        int duration = stopTimeEntry.getArrivalTime()
            - prevStopTimeEntry.getDepartureTime();

        if (duration < 0) {
          throw new IllegalStateException();
        }
      }
      
      prevStopTimeEntry = stopTimeEntry;

      sequence++;
    }
  }

  /**
   * The {@link StopTime#getArrivalTime()} and
   * {@link StopTime#getDepartureTime()} properties are optional. This method
   * takes charge of interpolating the arrival and departure time for any
   * StopTime where they are missing. The interpolation is based on the distance
   * traveled along the current trip/block.
   * 
   * @param stopTimes
   * @param distanceTraveled
   * @param arrivalTimes
   * @param departureTimes
   */
  private void interpolateArrivalAndDepartureTimes(List<StopTime> stopTimes,
      double[] distanceTraveled, int[] arrivalTimes, int[] departureTimes) {

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

  private double[] getDistanceTraveledForStopTimes(
      List<StopTimeEntryImpl> stopTimeEntries) {

    double[] distances = new double[stopTimeEntries.size()];

    for (int i = 0; i < stopTimeEntries.size(); i++) {
      StopTimeEntryImpl stopTime = stopTimeEntries.get(i);
      distances[i] = stopTime.getShapeDistTraveled();
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

  private static class StopTimeComparator implements Comparator<StopTime> {

    public int compare(StopTime o1, StopTime o2) {
      return o1.getStopSequence() - o2.getStopSequence();
    }
  }

}
