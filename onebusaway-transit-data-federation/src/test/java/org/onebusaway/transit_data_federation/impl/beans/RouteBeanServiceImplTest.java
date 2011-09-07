package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockTripIndices;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.impl.StopSequenceCollectionServiceImpl;
import org.onebusaway.transit_data_federation.impl.StopSequencesServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative.Builder;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.library.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

public class RouteBeanServiceImplTest {

  private RouteBeanServiceImpl _service;

  private ExtendedGtfsRelationalDao _gtfsDao;

  private TransitDataFederationDao _transitDataFederationDao;

  private AgencyBeanService _agencyBeanService;

  private RouteService _routeService;

  private ShapeBeanService _shapeBeanService;

  private StopSequencesServiceImpl _stopSequencesService;

  private StopBeanService _stopBeanService;

  private StopSequenceCollectionServiceImpl _stopSequenceBlocksService;

  private BlockIndexService _blockIndexService;

  private NarrativeService _narrativeService;

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

    _stopSequenceBlocksService = new StopSequenceCollectionServiceImpl();
    _service.setStopSequencesBlocksService(_stopSequenceBlocksService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _stopSequenceBlocksService.setNarrativeService(_narrativeService);

    _blockIndexService = Mockito.mock(BlockIndexService.class);
    _service.setBlockIndexService(_blockIndexService);
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

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.1, -122.1);
    StopEntryImpl stopC = stop("stopC", 47.2, -122.2);

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "sidA");
    TripEntryImpl tripB = trip("tripB", "sidA");

    tripA.setRouteCollectionId(routeId);
    tripA.setDirectionId("0");
    tripB.setRouteCollectionId(routeId);
    tripB.setDirectionId("1");
    
    Builder tnA = TripNarrative.builder();
    tnA.setTripHeadsign("Destination A");
    Mockito.when(_narrativeService.getTripForId(tripA.getId())).thenReturn(
        tnA.create());

    Builder tnB = TripNarrative.builder();
    tnB.setTripHeadsign("Destination B");
    Mockito.when(_narrativeService.getTripForId(tripB.getId())).thenReturn(
        tnB.create());

    stopTime(0, stopA, tripA, time(9, 00), time(9, 00), 0);
    stopTime(1, stopB, tripA, time(9, 30), time(9, 30), 100);
    stopTime(2, stopC, tripA, time(10, 00), time(10, 00), 200);
    stopTime(3, stopC, tripB, time(11, 30), time(11, 30), 0);
    stopTime(4, stopA, tripB, time(12, 30), time(12, 30), 200);

    linkBlockTrips(blockA, tripA, tripB);

    List<BlockTripIndex> blockIndices = blockTripIndices(blockA);
    Mockito.when(
        _blockIndexService.getBlockTripIndicesForRouteCollectionId(routeId)).thenReturn(
        blockIndices);

    StopBean stopBeanA = getStopBean(stopA);
    StopBean stopBeanB = getStopBean(stopB);
    StopBean stopBeanC = getStopBean(stopC);

    List<AgencyAndId> stopIds = Arrays.asList(stopA.getId(), stopB.getId(),
        stopC.getId());
    Mockito.when(_routeService.getStopsForRouteCollection(routeId)).thenReturn(
        stopIds);

    Mockito.when(_stopBeanService.getStopForId(stopA.getId())).thenReturn(
        stopBeanA);
    Mockito.when(_stopBeanService.getStopForId(stopB.getId())).thenReturn(
        stopBeanB);
    Mockito.when(_stopBeanService.getStopForId(stopC.getId())).thenReturn(
        stopBeanC);

    AgencyAndId shapeId = new AgencyAndId("1", "shapeId");

    List<AgencyAndId> shapeIds = Arrays.asList(shapeId);
    Mockito.when(_gtfsDao.getShapePointIdsForRoutes(routes)).thenReturn(
        shapeIds);

    EncodedPolylineBean polyline = new EncodedPolylineBean();
    Mockito.when(_shapeBeanService.getMergedPolylinesForShapeIds(shapeIds)).thenReturn(
        Arrays.asList(polyline));

    /*
     * Trip tripA = new Trip(); tripA.setId(new AgencyAndId("1", "tripA"));
     * tripA.setTripHeadsign("Destination A"); tripA.setDirectionId("0");
     * List<StopTime> stopTimesA = getStopTimesForStops(stopA, stopB, stopC);
     * 
     * Trip tripB = new Trip(); tripB.setId(new AgencyAndId("1", "tripB"));
     * tripB.setTripHeadsign("Destination B"); tripB.setDirectionId("1");
     * List<StopTime> stopTimesB = getStopTimesForStops(stopC, stopA);
     * 
     * List<Trip> trips = Arrays.asList(tripA, tripB);
     * 
     * Mockito.when(_gtfsDao.getTripsForRoute(route)).thenReturn(trips);
     * Mockito.when(_gtfsDao.getStopTimesForTrip(tripA)).thenReturn(stopTimesA);
     * Mockito.when(_gtfsDao.getStopTimesForTrip(tripB)).thenReturn(stopTimesB);
     */

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
    assertEquals(ids(stopA.getId(), stopB.getId(), stopC.getId()), stopIdsA);

    NameBean nameB = groupB.getName();
    assertEquals("destination", nameB.getType());
    assertEquals("Destination B", nameB.getName());

    List<String> stopIdsB = groupB.getStopIds();
    assertEquals(2, stopIdsB.size());
    assertEquals(ids(stopC.getId(), stopA.getId()), stopIdsB);

  }

  private StopBean getStopBean(StopEntryImpl stopEntry) {
    StopBean stop = new StopBean();
    stop.setId(AgencyAndIdLibrary.convertToString(stopEntry.getId()));
    return stop;
  }

  private List<String> ids(AgencyAndId... ids) {
    List<String> stringIds = new ArrayList<String>();
    for (AgencyAndId id : ids)
      stringIds.add(AgencyAndIdLibrary.convertToString(id));
    return stringIds;
  }
}
