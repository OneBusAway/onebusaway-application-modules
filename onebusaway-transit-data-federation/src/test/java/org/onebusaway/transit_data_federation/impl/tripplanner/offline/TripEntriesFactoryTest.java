package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.shapePoint;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class TripEntriesFactoryTest {

  @Test
  public void test() {

    TripPlannerGraphImpl graph = new TripPlannerGraphImpl();
    GtfsRelationalDaoImpl gtfsDao = new GtfsRelationalDaoImpl();

    ShapePointsTemporaryService shapePointsService = new ShapePointsTemporaryService();
    shapePointsService.setGtfsDao(gtfsDao);

    Agency agency = new Agency();
    agency.setId("1");
    agency.setTimezone("America/Los_Angeles");
    gtfsDao.saveEntity(agency);

    Route route = new Route();
    route.setId(new AgencyAndId("1", "routeA"));
    route.setAgency(agency);
    gtfsDao.saveEntity(route);

    AgencyAndId shapeId = new AgencyAndId("1", "shapeId");

    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "tripA"));
    trip.setRoute(route);
    trip.setServiceId(new AgencyAndId("1", "serviceId"));
    trip.setShapeId(shapeId);
    gtfsDao.saveEntity(trip);

    Stop stopA = new Stop();
    stopA.setId(aid("stopA"));
    graph.putStopEntry(stop("stopA", 47.672207391799056, -122.387855896286));

    Stop stopB = new Stop();
    stopB.setId(aid("stopB"));
    graph.putStopEntry(stop("stopB", 47.66852277218285, -122.3853882639923));

    graph.refreshStopMapping();

    StopTime stA = new StopTime();
    stA.setId(100);
    stA.setArrivalTime(time(9, 00));
    stA.setDepartureTime(time(9, 05));
    stA.setStopSequence(100);
    stA.setStop(stopA);
    stA.setTrip(trip);
    gtfsDao.saveEntity(stA);

    StopTime stB = new StopTime();
    stB.setId(101);
    stB.setArrivalTime(time(10, 00));
    stB.setDepartureTime(time(10, 05));
    stB.setStopSequence(102);
    stB.setStop(stopB);
    stB.setTrip(trip);
    gtfsDao.saveEntity(stB);

    gtfsDao.saveEntity(shapePoint("shapeId", 1, 47.673840100841396,
        -122.38756621771239));
    gtfsDao.saveEntity(shapePoint("shapeId", 2, 47.668667271970484,
        -122.38756621771239));
    gtfsDao.saveEntity(shapePoint("shapeId", 3, 47.66868172192725,
        -122.3661729186096));

    TripEntriesFactory factory = new TripEntriesFactory();
    factory.setGtfsDao(gtfsDao);
    factory.setShapePointsService(shapePointsService);
    factory.setUniqueService(new UniqueServiceImpl());

    RouteCollection rc = new RouteCollection();
    rc.setId(new AgencyAndId("1", "A"));
    TransitDataFederationDao whereDao = Mockito.mock(TransitDataFederationDao.class);
    Mockito.when(whereDao.getRouteCollectionForRoute(route)).thenReturn(rc);
    factory.setWhereDao(whereDao);

    StopTimeEntriesFactory stopTimeEntriesFactory = new StopTimeEntriesFactory();
    stopTimeEntriesFactory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    factory.setStopTimeEntriesFactory(stopTimeEntriesFactory);

    factory.processTrips(graph);

    TripEntryImpl entry = graph.getTripEntryForId(trip.getId());
    assertEquals(trip.getId(), entry.getId());
    assertEquals(new AgencyAndId("1", "A"), entry.getRouteCollectionId());
    assertEquals(route.getId(), entry.getRouteId());
    assertEquals(lsid("serviceId"), entry.getServiceId());
    assertEquals(trip.getShapeId(), entry.getShapeId());
    assertEquals(2177.1, entry.getTotalTripDistance(), 0.1);

    List<StopTimeEntry> stopTimes = entry.getStopTimes();
    assertEquals(2, stopTimes.size());

    for (StopTimeEntry stopTime : stopTimes) {
      assertSame(entry, stopTime.getTrip());
    }
  }
}
