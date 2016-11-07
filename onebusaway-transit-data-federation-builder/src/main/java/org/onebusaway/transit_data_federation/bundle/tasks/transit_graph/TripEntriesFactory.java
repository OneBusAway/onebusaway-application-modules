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

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.bundle.tasks.ShapePointHelper;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
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

  private boolean _throwExceptionOnInvalidStopToShapeMappingException = true;

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

    Collection<Route> routes = _gtfsDao.getAllRoutes();
    int routeIndex = 0;

    for (Route route : routes) {

      _log.info("route processed: " + routeIndex + "/" + routes.size());
      routeIndex++;

      List<Trip> tripsForRoute = _gtfsDao.getTripsForRoute(route);
      
      int tripCount = tripsForRoute.size();
      int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(tripCount);

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
    }

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

  private TripEntryImpl processTrip(TransitGraphImpl graph, Trip trip) {

    List<StopTime> stopTimes = _gtfsDao.getStopTimesForTrip(trip);

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

    List<StopTimeEntryImpl> stopTimesForTrip = _stopTimeEntriesFactory.processStopTimes(
        graph, stopTimes, tripEntry, shapePoints);

    // Also:  only set the trip if there are stops for it
    if (stopTimesForTrip == null || stopTimesForTrip.size() < 2) {
      _log.error("trip " + trip.getId() + " missing stops!");
      return null;
    }
    
    double tripDistance = getTripDistance(stopTimesForTrip, shapePoints);
    tripEntry.setTotalTripDistance(tripDistance);

    tripEntry.setStopTimes(cast(stopTimesForTrip));

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

  private double getTripDistance(List<StopTimeEntryImpl> stopTimes,
      ShapePoints shapePoints) {

    StopTimeEntryImpl lastStopTime = null;
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
}
