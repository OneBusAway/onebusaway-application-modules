/**
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.DistanceAlongShapeException;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.StopIsTooFarFromShapeException;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopTimeEntriesFactory {

  private static final boolean DEFAULT_LENIENT_MODE = false;
  private static final boolean DEFAULT_TIMEPOINT_SUPPORT = false;
  

  private Logger _log = LoggerFactory.getLogger(StopTimeEntriesFactory.class);

  private DistanceAlongShapeLibrary _distanceAlongShapeLibrary;

  private long _invalidStopToShapeMappingExceptionCount;
  
  private boolean isLenientArrivalDepartureTimes = DEFAULT_LENIENT_MODE;

  // OBA doesn't support time points very well -- so optionally remove them
  private boolean removeTimePoints = !DEFAULT_TIMEPOINT_SUPPORT;

  /**
   * set true if system should try to correct small errors in arrival/departure times
   * @param isLenient
   */
  @ConfigurationParameter
  public void setLenientArrivalDepartureTimes(boolean isLenient) {
    isLenientArrivalDepartureTimes = isLenient;
  }

  
  @Autowired
  public void setDistanceAlongShapeLibrary(
      DistanceAlongShapeLibrary distanceAlongShapeLibrary) {
    _distanceAlongShapeLibrary = distanceAlongShapeLibrary;
  }

  public long getInvalidStopToShapeMappingExceptionCount() {
    return _invalidStopToShapeMappingExceptionCount;
  }

  public List<StopTimeEntryImpl> processStopTimes(TransitGraphImpl graph,
      List<StopTime> stopTimes, TripEntryImpl tripEntry, ShapePoints shapePoints) {

    // In case the list is unmodifiable
    //stopTimes = new ArrayList<StopTime>(stopTimes);
	ArrayList<StopTime> newStopTimes = new ArrayList<StopTime>(stopTimes.size());
	
	for (StopTime stopTime : stopTimes) {
		if (stopTime != null && stopTime.getStop() != null 
				&& stopTime.getStop().getId() != null) {
			newStopTimes.add(stopTime);
		} else {
			_log.error("found stopTime without a stop id for tripEntry=" + tripEntry
					+ "(" + (stopTime.getTrip() == null?"NuLl":stopTime.getTrip().getId()) +")"
					+ "; stop=" + stopTime.getStopHeadsign() + ":" + StopTimeFieldMappingFactory.getSecondsAsString(stopTime.getArrivalTime()) + ":"
					+ StopTimeFieldMappingFactory.getSecondsAsString(stopTime.getDepartureTime()) + ":" + stopTime.getRouteShortName()
					+ "; route=" + (tripEntry.getRoute() == null?"NuLl":tripEntry.getRoute().getId())
					+ "; blockId=" + (tripEntry.getBlock() == null?"NuLl":tripEntry.getBlock().getId())
					+ "; stop=" + (stopTime.getStop() == null?"NuLl":stopTime.getId() + ":" + stopTime.getRouteShortName())
                    + "; stopTime=" + stopTime.toString()
            );
		}
	}

	stopTimes = newStopTimes;
    Collections.sort(stopTimes, new StopTimeComparator());

    if (removeTimePoints) {
    	stopTimes = removeTimePoints(stopTimes);
    }
    
    List<StopTimeEntryImpl> stopTimeEntries = createInitialStopTimeEntries(
        graph, stopTimes);

    for (StopTimeEntryImpl stopTime : stopTimeEntries) {
      stopTime.setTrip(tripEntry);
      stopTime.setTotalStopsInTrip(stopTimeEntries.size());
    }

    ensureStopTimesHaveShapeDistanceTraveledSet(stopTimeEntries, shapePoints);

    removeDuplicateStopTimes(stopTimes);
    ensureStopTimesHaveTimesSet(stopTimes, stopTimeEntries);

    return stopTimeEntries;
  }

  private List<StopTime> removeTimePoints(List<StopTime> stopTimes) {
	  List<StopTime> results = new ArrayList<StopTime>(stopTimes.size());
	  for (StopTime st : stopTimes) {
		  if (st.getDropOffType() == 1 && st.getPickupType() == 1) {
			  // we have a timepoint -- silently drop
		  } else {
			  results.add(st);
		  }
	  }
	  return results;
}


private void removeDuplicateStopTimes(List<StopTime> stopTimes) {
    Collections.sort(stopTimes, new StopTimeComparator());

    boolean stopTimeWasModified = false;
    StopTime lastUnmodifiedStopTime = null;

    for (StopTime stopTime : stopTimes) {
      stopTimeWasModified = false;

      if (lastUnmodifiedStopTime == null) {
        lastUnmodifiedStopTime = stopTime;
        continue;
      }

      if (lastUnmodifiedStopTime.isArrivalTimeSet()
          && stopTime.isArrivalTimeSet()) {
        if (stopTime.getArrivalTime() == lastUnmodifiedStopTime.getArrivalTime()) {
          stopTime.clearArrivalTime();
          stopTimeWasModified = true;
        }
      }

      if (lastUnmodifiedStopTime.isDepartureTimeSet()
          && stopTime.isDepartureTimeSet()) {
        if (stopTime.getDepartureTime() == lastUnmodifiedStopTime.getDepartureTime()) {
          stopTime.clearDepartureTime();
          stopTimeWasModified = true;
        }
      }

      // always compare to the last stop time we didn't change
      if (!stopTimeWasModified) {
        lastUnmodifiedStopTime = stopTime;
      }
    }
  }

  private List<StopTimeEntryImpl> createInitialStopTimeEntries(
      TransitGraphImpl graph, List<StopTime> stopTimes) {

    List<StopTimeEntryImpl> stopTimeEntries = new ArrayList<StopTimeEntryImpl>(
        stopTimes.size());
    int sequence = 0;

    for (StopTime stopTime : stopTimes) {
      if (stopTime == null) {
    	  _log.error("Found null stopTime in stopTime=" + stopTimes);
    	  continue;
      }
      StopLocation stop = stopTime.getStop();
      if (stop == null) {
    	  _log.error("Stop is null for stopTime" + stopTime.getId());
    	  continue;
      }
      if (stop.getId() == null) {
    	  _log.error("Stop id is null for stopTime" + stopTime + ", stop=" + stop);
    	  continue;
    	  
      }
      AgencyAndId stopId = stop.getId();
      StopEntryImpl stopEntry = graph.getStopEntryForId(stopId);

      StopTimeEntryImpl stopTimeEntry = new StopTimeEntryImpl();

      stopTimeEntry.setId(stopTime.getId());
      stopTimeEntry.setSequence(sequence);
      stopTimeEntry.setGtfsSequence(stopTime.getStopSequence());
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

    boolean distanceTraveledSet = false;

    // Do we have shape information?
    if (shapePoints != null) {

      try {
        PointAndIndex[] stopTimePoints = _distanceAlongShapeLibrary.getDistancesAlongShape(
            shapePoints, stopTimes);
        for (int i = 0; i < stopTimePoints.length; i++) {
          PointAndIndex pindex = stopTimePoints[i];
          StopTimeEntryImpl stopTime = stopTimes.get(i);
          stopTime.setShapePointIndex(pindex.index);
          stopTime.setShapeDistTraveled(pindex.distanceAlongShape);
        }

        distanceTraveledSet = true;
      } catch (StopIsTooFarFromShapeException ex) {
        StopTimeEntry stopTime = ex.getStopTime();
        TripEntry trip = stopTime.getTrip();
        StopEntry stop = stopTime.getStop();
        AgencyAndId shapeId = trip.getShapeId();
        CoordinatePoint point = ex.getPoint();
        PointAndIndex pindex = ex.getPointAndIndex();

        _log.warn("Stop is too far from shape: trip=" + trip.getId() + " stop="
            + stop.getId() + " stopLat=" + stop.getStopLat() + " stopLon="
            + stop.getStopLon() + " shapeId=" + shapeId + " shapePoint="
            + point + " index=" + pindex.index + " distance="
            + pindex.distanceFromTarget);
      } catch (DistanceAlongShapeException ex) {
        _invalidStopToShapeMappingExceptionCount++;
      } catch (IllegalArgumentException iae) {
        _log.warn("Stop has illegal coordinates along shapes=" + shapePoints);
      }
    }

    if (!distanceTraveledSet) {

      // Make do without
      double d = 0;
      StopTimeEntryImpl prev = null;
      for (StopTimeEntryImpl stopTime : stopTimes) {
        if (prev != null) {
          CoordinatePoint from = prev.getStop().getStopLocation();
          CoordinatePoint to = stopTime.getStop().getStopLocation();
          d += SphericalGeometryLibrary.distance(from, to);
        }
        stopTime.setShapeDistTraveled(d);
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
        	_log.error("Invalid duration of " + duration + " for stopTime " + stopTimeEntry.getId()
        			+ " of " + stopTimeEntry.getArrivalTime() 
        			+ " compared to previous stopTime departure " + prevStopTimeEntry.getId()
        			+ " of " + prevStopTimeEntry.getDepartureTime());
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
        throw new IllegalStateException(
            "departure time is less than arrival time for stop time with trip_id="
                + stopTime.getTrip().getId() + " stop_sequence="
                + stopTime.getStopSequence());

      if (i > 0 && arrivalTimes[i] < departureTimes[i - 1]) {

        /**
         * The previous stop time's departure time comes AFTER this stop time's
         * arrival time. That's bad.
         */
        StopTime prevStopTime = stopTimes.get(i - 1);
        StopLocation prevStop = prevStopTime.getStop();
        StopLocation stop = stopTime.getStop();

        if (prevStop.equals(stop)
            && arrivalTimes[i] == departureTimes[i - 1] - 1) {
          _log.info("fixing decreasing passingTimes: stopTimeA="
              + prevStopTime.getId() + " stopTimeB=" + stopTime.getId());
          arrivalTimes[i] = departureTimes[i - 1];
          if (departureTimes[i] < arrivalTimes[i])
            departureTimes[i] = arrivalTimes[i];
        } else if (isLenientMode() && i > 0 && (arrivalTimes[i] < arrivalTimes[i - 1]) && (departureTimes[i] < departureTimes[i - 1])) { 
          // recalculate the last two stops and hope for the best!
          // we can correct small accuracy errors here 
          int t0 =  (int) InterpolationLibrary.interpolate(
              scheduleTimesByDistanceTraveled, distanceTraveled[i-1]);
          int t1 = (int) InterpolationLibrary.interpolate(
              scheduleTimesByDistanceTraveled, distanceTraveled[i]);
          
          if (t1 < t0) {
        	  // sometimes even the interpolation gets it wrong
        	  int m = t0;
        	  t0 = t1;
        	  t1 = m;
        	  _log.warn("interpolation error");
          }
          _log.warn("correcting arrival time of sequence " + (stopTime.getStopSequence()-1) + ", " + stopTime.getStopSequence() +
              " of trip " + stopTime.getTrip().getId() + " as it was less than last departure time.  Arrival[" + (i-1) + "] " +
              arrivalTimes[i-1] + " now "+ t0 + ", Arrival[" + i + "] " + arrivalTimes[i] + " now " + t1);
          arrivalTimes[i-1] = departureTimes[i-1] = t0;
          arrivalTimes[i] = departureTimes[i] = t1; 
      } else {
          for (int x = 0; x < stopTimes.size(); x++) {
            StopTime st = stopTimes.get(x);
            final String msg = x + " " + st.getId() + " " + arrivalTimes[x]
                + " " + departureTimes[x];
            _log.error(msg);
            System.err.println(msg);
          }
          final String exceptionMessage = "arrival time is less than previous departure time for stop time " +
          " with isLenientArrivalDepartureTimes=" + this.isLenientArrivalDepartureTimes + " and trip_id="
              + stopTime.getTrip().getId() + " stop_sequence="
              + stopTime.getStopSequence() + ", arrivalTime=" + arrivalTimes[i] + ", departureTime=" + departureTimes[i] +
              (i>0?" arrivalTimes[" + (i-1) + "]=" + arrivalTimes[i-1] + ", departureTimes[" + (i-1) + "]=" + departureTimes[i-1]:" (i<1)");
          _log.error(exceptionMessage);
            throw new IllegalStateException(exceptionMessage);
        }
      }
    }
  }

  private boolean isLenientMode() {
    if (Boolean.TRUE.equals(isLenientArrivalDepartureTimes))
      return true;
    return false;
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
   * @param scheduleTimesByDistanceTraveled
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
