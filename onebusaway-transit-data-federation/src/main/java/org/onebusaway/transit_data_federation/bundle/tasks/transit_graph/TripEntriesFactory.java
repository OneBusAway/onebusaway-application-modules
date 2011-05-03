package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(TripEntriesFactory.class);

  private UniqueService _uniqueService;

  private TransitDataFederationDao _whereDao;

  private GtfsRelationalDao _gtfsDao;

  private StopTimeEntriesFactory _stopTimeEntriesFactory;

  private ShapePointService _shapePointsService;

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  @Autowired
  public void setWhereDao(TransitDataFederationDao whereDao) {
    _whereDao = whereDao;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointsService) {
    _shapePointsService = shapePointsService;
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

  public void processTrips(TransitGraphImpl graph) {

    Collection<Route> routes = _gtfsDao.getAllRoutes();
    int routeIndex = 0;

    for (Route route : routes) {

      _log.info("route processed: " + routeIndex + "/" + routes.size());
      routeIndex++;

      List<Trip> tripsForRoute = _gtfsDao.getTripsForRoute(route);

      _log.info("trips to process: " + tripsForRoute.size());
      int tripIndex = 0;
      RouteCollection routeCollection = _whereDao.getRouteCollectionForRoute(route);

      for (Trip trip : tripsForRoute) {
        if (tripIndex % 50 == 0)
          _log.info("trips processed: " + tripIndex + "/"
              + tripsForRoute.size());
        tripIndex++;
        TripEntryImpl tripEntry = processTrip(graph, trip);
        if (tripEntry != null)
          tripEntry.setRouteCollectionId(unique(routeCollection.getId()));
      }
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
      shapePoints = _shapePointsService.getShapePointsForShapeId(trip.getShapeId());

    Agency agency = trip.getRoute().getAgency();
    TimeZone tz = TimeZone.getTimeZone(agency.getTimezone());
    LocalizedServiceId lsid = new LocalizedServiceId(trip.getServiceId(), tz);

    TripEntryImpl tripEntry = new TripEntryImpl();

    tripEntry.setId(trip.getId());
    tripEntry.setRouteId(unique(trip.getRoute().getId()));
    tripEntry.setDirectionId(unique(trip.getDirectionId()));
    tripEntry.setServiceId(unique(lsid));

    // Only set the shape id for a trip if there are actually shape points to
    // back it up
    if (!(shapePoints == null || shapePoints.isEmpty()))
      tripEntry.setShapeId(unique(trip.getShapeId()));

    List<StopTimeEntryImpl> stopTimesForTrip = _stopTimeEntriesFactory.processStopTimes(
        graph, stopTimes, tripEntry, shapePoints);

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

    StopTimeEntryImpl lastStopTime = stopTimes.get(stopTimes.size() - 1);

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
