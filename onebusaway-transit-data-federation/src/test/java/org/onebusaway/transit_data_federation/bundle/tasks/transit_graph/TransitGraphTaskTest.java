package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.UniqueServiceImpl;
import org.onebusaway.transit_data_federation.impl.refresh.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;

public class TransitGraphTaskTest {

  @Test
  public void testInterpolation() throws IOException, ClassNotFoundException {

    /****
     * Normally Spring will take care of wiring all this up
     ****/

    GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    CalendarServiceData calendarData = new CalendarServiceData();
    calendarService.setData(calendarData);

    TransitDataFederationDao whereDao = Mockito.mock(TransitDataFederationDao.class);

    UniqueServiceImpl uniqueService = new UniqueServiceImpl();

    ServiceIdOverlapCache serviceIdOverlapCache = new ServiceIdOverlapCache();
    serviceIdOverlapCache.setCalendarService(calendarService);

    ShapePointServiceImpl shapePointsService = new ShapePointServiceImpl();
    shapePointsService.setGtfsDao(gtfsDao);

    BlockConfigurationEntriesFactory blockConfigurationEntriesFactory = new BlockConfigurationEntriesFactory();
    blockConfigurationEntriesFactory.setServiceIdOverlapCache(serviceIdOverlapCache);
    blockConfigurationEntriesFactory.setShapePointService(shapePointsService);

    BlockEntriesFactory blockEntriesFactory = new BlockEntriesFactory();
    blockEntriesFactory.setGtfsDao(gtfsDao);
    blockEntriesFactory.setBlockConfigurationEntriesFactory(blockConfigurationEntriesFactory);

    StopEntriesFactory stopEntriesFactory = new StopEntriesFactory();
    stopEntriesFactory.setGtfsDao(gtfsDao);

    StopTimeEntriesFactory stopTimeEntriesFactory = new StopTimeEntriesFactory();
    stopTimeEntriesFactory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    TripEntriesFactory tripEntriesFactory = new TripEntriesFactory();
    tripEntriesFactory.setGtfsDao(gtfsDao);
    tripEntriesFactory.setStopTimeEntriesFactory(stopTimeEntriesFactory);
    tripEntriesFactory.setShapePointService(shapePointsService);
    tripEntriesFactory.setUniqueService(uniqueService);
    tripEntriesFactory.setWhereDao(whereDao);

    TransitGraphTask task = new TransitGraphTask();
    task.setBlockEntriesFactory(blockEntriesFactory);
    task.setStopEntriesFactory(stopEntriesFactory);
    task.setTripEntriesFactory(tripEntriesFactory);

    RefreshService refreshService = Mockito.mock(RefreshService.class);
    task.setRefreshService(refreshService);

    /****
     * Add actual data
     ****/

    // This is a trip that visits the same point twice, which makes a good test

    double[] coords = {40.677665, -73.96886, // vanderbilt at prospect
    40.67706, -73.965974, // underhill at prospect
        40.67618, -73.966296, // underhill at park
        40.676785, -73.969178, // vanderbilt at park
        40.677665, -73.96886, // vanderbilt at prospect again
        40.678543, -73.968542, // vanderbilt at st mark's
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
      gtfsDao.saveEntity(point);
    }

    Stop stop1 = new Stop();
    stop1.setId(new AgencyAndId("agency", "stop1"));
    stop1.setLat(coords[0]);
    stop1.setLon(coords[1]);
    gtfsDao.saveEntity(stop1);

    Stop stop2 = new Stop();
    stop2.setId(new AgencyAndId("agency", "stop2"));
    stop2.setLat(coords[4]);
    stop2.setLon(coords[5]);
    gtfsDao.saveEntity(stop2);

    Stop stop3 = new Stop();
    stop3.setId(new AgencyAndId("agency", "stop3"));
    stop3.setLat(coords[10]);
    stop3.setLon(coords[11]);
    gtfsDao.saveEntity(stop3);

    Agency agency = new Agency();
    agency.setId("agency");
    agency.setTimezone("America/New_York");
    gtfsDao.saveEntity(agency);

    Route route = new Route();
    route.setAgency(agency);
    route.setId(new AgencyAndId("agency", "route"));
    gtfsDao.saveEntity(route);

    Trip trip = new Trip();
    AgencyAndId tripId = new AgencyAndId("agency", "trip");
    AgencyAndId serviceId = new AgencyAndId("agency", "service");
    trip.setRoute(route);
    trip.setId(tripId);
    trip.setShapeId(shapeId);
    trip.setServiceId(serviceId);
    gtfsDao.saveEntity(trip);

    calendarData.putTimeZoneForAgencyId("agency", TimeZone.getDefault());

    StopTime st1 = new StopTime();
    st1.setStop(stop1);
    StopTime st2 = new StopTime();
    st2.setStop(stop2);
    StopTime st3 = new StopTime();
    st3.setStop(stop1);
    StopTime st4 = new StopTime();
    st4.setStop(stop3);

    List<StopTime> stopTimes = new ArrayList<StopTime>();
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
      gtfsDao.saveEntity(stopTime);
    }

    FederatedTransitDataBundle bundle = Mockito.mock(FederatedTransitDataBundle.class);
    task.setBundle(bundle);

    File tmpFile = File.createTempFile("testgraph", "graph");
    Mockito.when(bundle.getTransitGraphPath()).thenReturn(tmpFile);

    RouteCollection routeCollection = new RouteCollection();
    routeCollection.setRoutes(Arrays.asList(route));

    Mockito.when(whereDao.getRouteCollectionForRoute(route)).thenReturn(
        routeCollection);

    /****
     * Pull the Trigger
     ****/

    task.run();

    TransitGraphImpl graph = ObjectSerializationLibrary.readObject(tmpFile);
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

    Mockito.verify(refreshService).refresh(RefreshableResources.TRANSIT_GRAPH);
  }
}
