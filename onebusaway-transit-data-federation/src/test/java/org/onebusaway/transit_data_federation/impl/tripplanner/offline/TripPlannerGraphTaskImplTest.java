package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripPlannerGraphTaskImplTest {
  @Test
  public void testInterpolation() throws IOException, ClassNotFoundException {

    TripPlannerGraphTaskImpl task = new TripPlannerGraphTaskImpl();
    
    ExtendedGtfsRelationalDao gtfsDao = Mockito.mock(ExtendedGtfsRelationalDao.class);
    task.setGtfsDao(gtfsDao);
    
    //This is a trip that visits the same point twice, which makes a good test 
    
    double[] coords = {40.677665, -73.96886, //vanderbilt at prospect
                       40.67706, -73.965974, //underhill at prospect
                       40.67618, -73.966296, //underhill at park
                       40.676785, -73.969178, //vanderbilt at park
                       40.677665, -73.96886, //vanderbilt at prospect again
                       40.678543, -73.968542, //vanderbilt at st mark's
                       };

    AgencyAndId shapeId = new AgencyAndId("agency", "shape");

    List<ShapePoint> points = new ArrayList<ShapePoint>();
    for (int i = 0; i < coords.length; i += 2) {
      ShapePoint point = new ShapePoint();
      point.setLat(coords[i]);
      point.setLon(coords[i + 1]);
      point.setSequence(i);
      point.setShapeId(shapeId);
      points.add(point);
    }
    Mockito.when(gtfsDao.getShapePointsForShapeId(shapeId)).thenReturn(
        points );
    
    List<Stop> stops = new ArrayList<Stop>();
    Stop stop1 = new Stop();
    stop1.setId(new AgencyAndId("agency", "stop1"));
    stop1.setLat(coords[0]);
    stop1.setLon(coords[1]);
    stops.add(stop1);
    
    Stop stop2 = new Stop();
    stop2.setId(new AgencyAndId("agency", "stop2"));
    stop2.setLat(coords[4]);
    stop2.setLon(coords[5]);
    stops.add(stop2);
    
    Stop stop3 = new Stop();
    stop3.setId(new AgencyAndId("agency", "stop3"));
    stop3.setLat(coords[10]);
    stop3.setLon(coords[11]);
    stops.add(stop3);
    
    Agency agency = new Agency();
    agency.setId("agency");
    agency.setTimezone("America/New_York");
    
    Mockito.when(gtfsDao.getAllStops()).thenReturn(stops);
    
    List<Route> routes = new ArrayList<Route>();
    Route route = new Route();
    route.setAgency(agency);
    route.setId(new AgencyAndId("agency", "route"));
    routes.add(route);
    Mockito.when(gtfsDao.getAllRoutes()).thenReturn(routes);
        
    List<Trip> trips = new ArrayList<Trip>();
    Trip trip = new Trip();
    trip.setRoute(route);
    AgencyAndId tripId = new AgencyAndId("agency", "trip");
    trip.setId(tripId);
    trip.setShapeId(shapeId);
    trips.add(trip);
    AgencyAndId serviceId = new AgencyAndId("agency", "service");
    trip.setServiceId(serviceId);
    Mockito.when(gtfsDao.getAllTrips()).thenReturn(trips);
    
    Mockito.when(gtfsDao.getTripsForRoute(route)).thenReturn(trips);
    
    FederatedTransitDataBundle bundle = Mockito.mock(FederatedTransitDataBundle.class);
    task.setBundle(bundle );
    
    Mockito.when(gtfsDao.getTripForId(tripId)).thenReturn(trip);
    List<StopTime> stopTimes = new ArrayList<StopTime>();
    StopTime st1 = new StopTime();
    st1.setStop(stop1);
    StopTime st2 = new StopTime();
    st2.setStop(stop2);
    StopTime st3 = new StopTime();
    st3.setStop(stop1);
    StopTime st4 = new StopTime();
    st4.setStop(stop3);
    stopTimes.add(st1);
    stopTimes.add(st2);
    stopTimes.add(st3);
    stopTimes.add(st4);
    int seq = 0;
    for (StopTime stopTime : stopTimes) {
      stopTime.setArrivalTime(seq * 100);
      stopTime.setDepartureTime(seq * 100);
      stopTime.setTrip(trip);
      stopTime.setStopSequence(seq++);
    }
    Mockito.when(gtfsDao.getStopTimesForTrip(trip)).thenReturn(stopTimes);
    
    
    File tmpFile = File.createTempFile("testgraph", "graph");
    Mockito.when(bundle.getTripPlannerGraphPath()).thenReturn(tmpFile);

    TransitDataFederationDao whereDao = Mockito.mock(TransitDataFederationDao.class);
    task.setTransitDataFederationDao(whereDao);
    RouteCollection routeCollection = new RouteCollection();
    routeCollection.setRoutes(Arrays.asList(route));
    
    Mockito.when(whereDao.getRouteCollectionForRoute(route)).thenReturn(
        routeCollection);
    
    SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
    Session session = Mockito.mock(Session.class);
    Mockito.when(sessionFactory.getCurrentSession()).thenReturn(session);
    task.setSessionFactory(sessionFactory);
    task.run();

    TripPlannerGraphImpl graph = ObjectSerializationLibrary.readObject(tmpFile);
    assertNotNull(graph);

    TripEntry tripEntry = graph.getTripEntryForId(tripId);
    List<StopTimeEntry> stopTimeEntries = tripEntry.getStopTimes();
    StopTimeEntry ste0 = stopTimeEntries.get(0);
    assertTrue(ste0.getShapeDistTraveled() == 0);
    StopTimeEntry ste1 = stopTimeEntries.get(1);
    assertTrue(ste1.getShapeDistTraveled() - 354.0509978719913 < 0.1);
    StopTimeEntry ste2 = stopTimeEntries.get(2);
    assertTrue(ste2.getShapeDistTraveled() - 707.6902344031623 < 0.1);
    StopTimeEntry ste3 = stopTimeEntries.get(03);
    assertTrue(ste3.getShapeDistTraveled() - 808.935496820855 < 0.1);

  }
}
