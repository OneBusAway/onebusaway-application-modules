package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.impl.StopSequenceBlocksServiceImpl;
import org.onebusaway.transit_data_federation.impl.StopSequencesServiceImpl;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.StopSequenceCollectionService;
import org.onebusaway.transit_data_federation.services.StopSequencesService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;

public class RouteBeanServiceImplTest {

  private RouteBeanServiceImpl _service;

  private ExtendedGtfsRelationalDao _gtfsDao;

  private TransitDataFederationDao _transitDataFederationDao;

  private AgencyBeanService _agencyBeanService;

  private RouteService _routeService;

  private ShapeBeanService _shapeBeanService;

  private StopSequencesService _stopSequencesService;

  private StopBeanService _stopBeanService;

  private StopSequenceCollectionService _stopSequenceBlocksService;

  @Before
  public void setup() {
    _service = new RouteBeanServiceImpl();

    _gtfsDao = Mockito.mock(ExtendedGtfsRelationalDao.class);
    _service.setGtfsDao(_gtfsDao);

    _transitDataFederationDao = Mockito.mock(TransitDataFederationDao.class);
    _service.setTransitDataFederationDao(_transitDataFederationDao);

    _agencyBeanService = Mockito.mock(AgencyBeanService.class);
    _service.setAgencyBeanService(_agencyBeanService);

    _routeService = Mockito.mock(RouteService.class);
    _service.setRouteService(_routeService);

    _shapeBeanService = Mockito.mock(ShapeBeanService.class);
    _service.setShapeBeanService(_shapeBeanService);

    _stopBeanService = Mockito.mock(StopBeanService.class);
    _service.setStopBeanService(_stopBeanService);

    _stopSequencesService = new StopSequencesServiceImpl();
    _service.setStopSequencesLibrary(_stopSequencesService);
    
    _stopSequenceBlocksService = new StopSequenceBlocksServiceImpl();
    _service.setStopSequencesBlocksService(_stopSequenceBlocksService);
  }

  @Test
  public void testGetRouteForId() {

    AgencyAndId routeId = new AgencyAndId("1", "route");

    RouteCollection route = new RouteCollection();
    route.setColor("blue");
    route.setDescription("route desc");
    route.setId(routeId);
    route.setLongName("route long name");
    route.setShortName("route short name");
    route.setTextColor("red");
    route.setType(3);
    route.setUrl("http://wwww.route.com");

    AgencyBean agency = new AgencyBean();
    Mockito.when(_agencyBeanService.getAgencyForId("1")).thenReturn(agency);

    Mockito.when(_transitDataFederationDao.getRouteCollectionForId(routeId)).thenReturn(
        route);

    RouteBean bean = _service.getRouteForId(routeId);

    assertEquals(route.getColor(), bean.getColor());
    assertEquals(route.getDescription(), bean.getDescription());
    assertEquals(AgencyAndIdLibrary.convertToString(routeId), bean.getId());
    assertEquals(route.getLongName(), bean.getLongName());
    assertEquals(route.getShortName(), bean.getShortName());
    assertEquals(route.getTextColor(), bean.getTextColor());
    assertEquals(route.getType(), bean.getType());
    assertEquals(route.getUrl(), bean.getUrl());
  }

  @Test
  public void testGetStopsForRoute() {

    AgencyAndId routeId = new AgencyAndId("1", "route");

    Route route = new Route();
    route.setId(new AgencyAndId("1", "raw_route"));
    List<Route> routes = Arrays.asList(route);

    RouteCollection routeCollection = new RouteCollection();
    routeCollection.setId(routeId);
    routeCollection.setRoutes(routes);

    Mockito.when(_transitDataFederationDao.getRouteCollectionForId(routeId)).thenReturn(
        routeCollection);

    AgencyAndId stopIdA = new AgencyAndId("1", "stopA");
    AgencyAndId stopIdB = new AgencyAndId("1", "stopB");
    AgencyAndId stopIdC = new AgencyAndId("1", "stopC");

    Stop stopA = getStop(stopIdA, 47.0, -122.0);
    Stop stopB = getStop(stopIdB, 47.1, -122.1);
    Stop stopC = getStop(stopIdC, 47.2, -122.2);

    StopBean stopBeanA = getStopBean(stopIdA);
    StopBean stopBeanB = getStopBean(stopIdB);
    StopBean stopBeanC = getStopBean(stopIdC);

    List<AgencyAndId> stopIds = Arrays.asList(stopIdA, stopIdB, stopIdC);
    Mockito.when(_routeService.getStopsForRouteCollection(routeId)).thenReturn(
        stopIds);

    Mockito.when(_stopBeanService.getStopForId(stopIdA)).thenReturn(stopBeanA);
    Mockito.when(_stopBeanService.getStopForId(stopIdB)).thenReturn(stopBeanB);
    Mockito.when(_stopBeanService.getStopForId(stopIdC)).thenReturn(stopBeanC);

    AgencyAndId shapeId = new AgencyAndId("1", "shapeId");

    List<AgencyAndId> shapeIds = Arrays.asList(shapeId);
    Mockito.when(_gtfsDao.getShapePointIdsForRoutes(routes)).thenReturn(
        shapeIds);

    EncodedPolylineBean polyline = new EncodedPolylineBean();
    Mockito.when(_shapeBeanService.getMergedPolylinesForShapeIds(shapeIds)).thenReturn(
        Arrays.asList(polyline));

    Trip tripA = new Trip();
    tripA.setId(new AgencyAndId("1", "tripA"));
    tripA.setTripHeadsign("Destination A");
    tripA.setDirectionId("0");
    List<StopTime> stopTimesA = getStopTimesForStops(stopA, stopB, stopC);

    Trip tripB = new Trip();
    tripB.setId(new AgencyAndId("1", "tripB"));
    tripB.setTripHeadsign("Destination B");
    tripB.setDirectionId("1");
    List<StopTime> stopTimesB = getStopTimesForStops(stopC, stopA);

    List<Trip> trips = Arrays.asList(tripA, tripB);

    Mockito.when(_gtfsDao.getTripsForRoute(route)).thenReturn(trips);
    Mockito.when(_gtfsDao.getStopTimesForTrip(tripA)).thenReturn(stopTimesA);
    Mockito.when(_gtfsDao.getStopTimesForTrip(tripB)).thenReturn(stopTimesB);

    // Setup complete

    StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);

    List<StopBean> stops = stopsForRoute.getStops();
    assertEquals(3, stops.size());
    assertSame(stopBeanA, stops.get(0));
    assertSame(stopBeanB, stops.get(1));
    assertSame(stopBeanC, stops.get(2));

    List<EncodedPolylineBean> polylines = stopsForRoute.getPolylines();
    assertEquals(1, polylines.size());
    assertSame(polyline, polylines.get(0));

    List<StopGroupingBean> groupings = stopsForRoute.getStopGroupings();
    assertEquals(1, groupings.size());
    StopGroupingBean grouping = groupings.get(0);
    assertEquals("direction", grouping.getType());

    List<StopGroupBean> groups = grouping.getStopGroups();
    assertEquals(2, groups.size());

    StopGroupBean groupA = groups.get(0);
    StopGroupBean groupB = groups.get(1);

    NameBean nameA = groupA.getName();
    assertEquals("destination", nameA.getType());
    assertEquals("Destination A", nameA.getName());

    List<String> stopIdsA = groupA.getStopIds();
    assertEquals(3, stopIdsA.size());
    assertEquals(ids(stopIdA, stopIdB, stopIdC), stopIdsA);

    NameBean nameB = groupB.getName();
    assertEquals("destination", nameB.getType());
    assertEquals("Destination B", nameB.getName());

    List<String> stopIdsB = groupB.getStopIds();
    assertEquals(2, stopIdsB.size());
    assertEquals(ids(stopIdC, stopIdA), stopIdsB);

  }

  private Stop getStop(AgencyAndId stopId, double lat, double lon) {
    Stop stop = new Stop();
    stop.setId(stopId);
    stop.setLat(lat);
    stop.setLon(lon);
    return stop;
  }

  private StopBean getStopBean(AgencyAndId stopId) {
    StopBean stop = new StopBean();
    stop.setId(AgencyAndIdLibrary.convertToString(stopId));
    return stop;
  }

  private List<StopTime> getStopTimesForStops(Stop... stops) {

    List<StopTime> stopTimes = new ArrayList<StopTime>();

    for (Stop stop : stops) {

      StopTime stopTime = new StopTime();
      stopTime.setStop(stop);

      stopTimes.add(stopTime);
    }

    return stopTimes;
  }

  private List<String> ids(AgencyAndId... ids) {
    List<String> stringIds = new ArrayList<String>();
    for (AgencyAndId id : ids)
      stringIds.add(AgencyAndIdLibrary.convertToString(id));
    return stringIds;
  }
}
