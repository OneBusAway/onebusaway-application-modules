/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.bundle.tasks.ShapePointHelper;
import org.onebusaway.transit_data_federation.impl.transit_graph.*;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(TripEntriesFactory.class);

  private UniqueService _uniqueService;

  private GtfsRelationalDao _gtfsDao;

  private StopTimeEntriesFactory _stopTimeEntriesFactory;

  private ShapePointHelper _shapePointsHelper;

  private ExecutorService _executor = null;

  private boolean _throwExceptionOnInvalidStopToShapeMappingException = false;

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  @Autowired
  public void setShapePointHelper(ShapePointHelper shapePointsHelper) {
    _shapePointsHelper = shapePointsHelper;
  }

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setStopTimeEntriesFactory(
      StopTimeEntriesFactory stopTimeEntriesFactory) {
    _stopTimeEntriesFactory = stopTimeEntriesFactory;
  }

  /**
   * By default, we throw an exception when an invalid stop-to-shape mapping is
   * found for a GTFS feed. Override that behavior by setting this parameter to
   * false.
   * 
   * @param throwExceptionOnInvalidStopToShapeMappingException when true, an
   *          exception is thrown on invalid stop-to-shape mappings
   */
  @ConfigurationParameter
  public void setThrowExceptionOnInvalidStopToShapeMappingException(
      boolean throwExceptionOnInvalidStopToShapeMappingException) {
    _throwExceptionOnInvalidStopToShapeMappingException = throwExceptionOnInvalidStopToShapeMappingException;
  }

  public void processTrips(TransitGraphImpl graph) {
    setupExecutor();
    Collection<Route> routes = _gtfsDao.getAllRoutes();
    int routeIndex = 0;
    List<JobResult> results = new ArrayList<>();
    for (Route route : routes) {
      JobResult result = new JobResult();
      routeIndex++;
      ProcessRouteJob jt = new ProcessRouteJob(graph, route, routeIndex, result);

      results.add(result);
      _executor.submit(jt);
    }

    waitOnExector(results);

    if (_stopTimeEntriesFactory.getInvalidStopToShapeMappingExceptionCount() > 0
        && _throwExceptionOnInvalidStopToShapeMappingException) {
      throw new IllegalStateException(
          "Multiple instances of InvalidStopToShapeMappingException thrown: count="
              + _stopTimeEntriesFactory.getInvalidStopToShapeMappingExceptionCount()
              + ".  For more information on errors of this kind, see:\n"
              + "  https://github.com/OneBusAway/onebusaway-application-modules/wiki/Stop-to-Shape-Matching");
    }

    graph.refreshTripMapping();
  }

  private void waitOnExector(List<JobResult> results) {
    int i = 0;
    try {
      for (JobResult result : results) {
        while (!result.isDone()) {
          try {
            _log.info("waiting on result {} of {}", i, results.size());
            Thread.sleep(1 * 1000);
          } catch (InterruptedException e) {
            _log.error("interrupted and exiting");
            return;
          }
        }
        i++;
      }
      _log.info("verified {} complete of {}", i, results.size());
    } finally {
      if (_executor != null) {
        try {
          _executor.shutdown();
          _executor.awaitTermination(1, TimeUnit.MINUTES);
          _executor.shutdownNow();
        } catch (Exception e) {
          return;
        }
      }
    }
  }

  private void setupExecutor() {
    if (_executor == null) {
      int cpus = Runtime.getRuntime().availableProcessors();
      _executor = Executors.newFixedThreadPool(cpus);
      _log.info("created threadpool of " + cpus);
    }
  }

  private void processRoute(TransitGraphImpl graph, Route route, int routeIndex) {
    List<Trip> tripsForRoute = _gtfsDao.getTripsForRoute(route);

    int tripCount = tripsForRoute.size();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(tripCount * 10); // slow down logging

    _log.info("trips to process: " + tripCount);
    int tripIndex = 0;
    RouteEntryImpl routeEntry = graph.getRouteForId(route.getId());
    ArrayList<TripEntry> tripEntries = new ArrayList<TripEntry>();

    for (Trip trip : tripsForRoute) {
      tripIndex++;
      if (tripIndex % logInterval == 0)
        _log.info("trips processed: " + tripIndex + "/"
                + tripsForRoute.size());
      TripEntryImpl tripEntry = processTrip(graph, trip);
      if (tripEntry != null) {
        tripEntry.setRoute(routeEntry);
        tripEntries.add(tripEntry);
      }
    }

    tripEntries.trimToSize();
    routeEntry.setTrips(tripEntries);
    _log.info("complete {}", routeIndex);
  }

  private TripEntryImpl processTrip(TransitGraphImpl graph, Trip trip) {
    List<StopTime> stopTimes = null;
    synchronized (_gtfsDao) {
      int maxTries = 40;
      int i = 0;
      while (stopTimes == null && i < maxTries) {
        // getStopTimesForTrip is not thread safe
        try {
          stopTimes = _gtfsDao.getStopTimesForTrip(trip);
        } catch (Throwable t) {
          _log.error("gtfsDao blew....", t, t);
          stopTimes = null;
        }
        if (stopTimes == null) {
          _log.error("processTrip failed for trip " + trip);
        }
        i++;
      }
    }

    // A trip without stop times is a trip we don't care about
    if (stopTimes.isEmpty())
      return null;

    ShapePoints shapePoints = null;

    if (trip.getShapeId() != null)
      shapePoints = _shapePointsHelper.getShapePointsForShapeId(trip.getShapeId());

    Agency agency = trip.getRoute().getAgency();
    TimeZone tz = TimeZone.getTimeZone(agency.getTimezone());
    LocalizedServiceId lsid = new LocalizedServiceId(trip.getServiceId(), tz);

    TripEntryImpl tripEntry = new TripEntryImpl();

    tripEntry.setId(trip.getId());
    tripEntry.setDirectionId(unique(trip.getDirectionId()));
    tripEntry.setServiceId(unique(lsid));

    // Only set the shape id for a trip if there are actually shape points to
    // back it up
    if (!(shapePoints == null || shapePoints.isEmpty()))
      tripEntry.setShapeId(unique(trip.getShapeId()));

    List<StopTimeEntry> stopTimesForTrip = _stopTimeEntriesFactory.processStopTimes(
        graph, stopTimes, tripEntry, shapePoints);

    // Also:  only set the trip if there are stops for it
    if (stopTimesForTrip == null || stopTimesForTrip.size() < 2) {
      _log.error("trip " + trip.getId() + " missing stops!");
      return null;
    }
    
    double tripDistance = getTripDistance(stopTimesForTrip, shapePoints);
    tripEntry.setTotalTripDistance(tripDistance);

    tripEntry.setStopTimes(stopTimesForTrip);

    graph.putTripEntry(tripEntry);

    return tripEntry;
  }

  private List<StopTimeEntry> cast(List<StopTimeEntryImpl> stopTimesForTrip) {
    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>(
        stopTimesForTrip.size());
    for (StopTimeEntryImpl stopTime : stopTimesForTrip)
      stopTimes.add(stopTime);
    return stopTimes;
  }

  private double getTripDistance(List<StopTimeEntry> stopTimes,
      ShapePoints shapePoints) {

    StopTimeEntry lastStopTime = null;
    try {
    lastStopTime = stopTimes.get(stopTimes.size() - 1);
    } catch (ArrayIndexOutOfBoundsException e) {
      _log.error("FATAL:  missing last stop " + stopTimes);
    }
    
    

    if (shapePoints != null) {
      double[] distances = shapePoints.getDistTraveled();
      double distance = distances[shapePoints.getSize() - 1];
      return Math.max(lastStopTime.getShapeDistTraveled(), distance);
    }

    return lastStopTime.getShapeDistTraveled();
  }

  private <T> T unique(T value) {
    return _uniqueService.unique(value);
  }

  public class ProcessRouteJob implements Runnable {
    private TransitGraphImpl graph;
    private Route route;
    private int routeIndex;
    private JobResult result;
    public ProcessRouteJob(TransitGraphImpl graph, Route route, int routeIndex, JobResult result) {
      this.graph = graph;
      this.route = route;
      this.routeIndex = routeIndex;
      this.result = result;
    }
    public void run() {
      try {
        processRoute(graph, route, routeIndex);
      } catch (Throwable t) {
        _log.error("pr blew {}", t, t);
      } finally {
        result.setDone();
      }
    }

  }

  public class JobResult {
    private boolean done = false;
    public void setDone() {
      done = true;
    }
    public boolean isDone() {
      return done;
    }
  }
}
