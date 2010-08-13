package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

public class GenerateRouteCollectionsTaskTest {

  @Test
  public void test() {

    GenerateRouteCollectionsTask task = new GenerateRouteCollectionsTask();

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    task.setGtfsDao(gtfsDao);

    TransitDataFederationMutableDao transitDao = Mockito.mock(TransitDataFederationMutableDao.class);
    task.setTransitDataFederationMutableDao(transitDao);

    Route routeA = route("a", "A", "Route A");
    Route routeB = route("b1", "B", "Route B Local");
    Route routeC = route("b2", "B", "Route B Express");

    List<Route> routes = Arrays.asList(routeA, routeB, routeC);
    Mockito.when(gtfsDao.getAllRoutes()).thenReturn(routes);

    Mockito.when(gtfsDao.getTripsForRoute(routeA)).thenReturn(trips(routeA, 5));
    Mockito.when(gtfsDao.getTripsForRoute(routeB)).thenReturn(trips(routeB, 10));
    Mockito.when(gtfsDao.getTripsForRoute(routeC)).thenReturn(trips(routeC, 6));

    task.run();

    ArgumentCaptor<RouteCollection> captor = ArgumentCaptor.forClass(RouteCollection.class);
    Mockito.verify(transitDao, Mockito.times(2)).save(captor.capture());

    List<RouteCollection> values = captor.getAllValues();
    assertEquals(2, values.size());

    RouteCollection rcA = values.get(0);
    RouteCollection rcB = values.get(1);

    if (rcA.getId().equals(new AgencyAndId("agency", "B"))) {
      RouteCollection temp = rcA;
      rcA = rcB;
      rcB = temp;
    }

    assertEquals(new AgencyAndId("agency", "A"), rcA.getId());
    assertEquals("A", rcA.getShortName());
    assertEquals("Route A", rcA.getLongName());

    assertEquals(new AgencyAndId("agency", "B"), rcB.getId());
    assertEquals("B", rcB.getShortName());
    assertEquals("Route B Local", rcB.getLongName());
  }

  private Route route(String id, String shortName, String longName) {
    Route route = new Route();
    route.setId(new AgencyAndId("agency", id));
    route.setShortName(shortName);
    route.setLongName(longName);
    return route;
  }

  private List<Trip> trips(Route route, int count) {

    AgencyAndId routeId = route.getId();
    List<Trip> trips = new ArrayList<Trip>();

    for (int i = 0; i < count; i++) {
      Trip trip = new Trip();
      trip.setId(new AgencyAndId(routeId.getAgencyId(), route.getId() + "-" + i));
      trip.setRoute(route);
      trips.add(trip);
    }

    return trips;
  }
}
