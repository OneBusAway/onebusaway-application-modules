package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

public class StopBeanServiceImplTest {

  private StopBeanServiceImpl _service;

  private GtfsRelationalDao _dao;

  private NarrativeService _narrativeService;

  private RouteService _routeService;

  private RouteBeanService _routeBeanService;

  @Before
  public void setup() {
    _service = new StopBeanServiceImpl();

    _dao = Mockito.mock(GtfsRelationalDao.class);
    _service.setGtfsDao(_dao);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

    _routeService = Mockito.mock(RouteService.class);
    _service.setRouteService(_routeService);

    _routeBeanService = Mockito.mock(RouteBeanService.class);
    _service.setRouteBeanService(_routeBeanService);
  }

  @Test
  public void testGetStopForId() {

    AgencyAndId stopId = new AgencyAndId("29", "1109");

    Stop stop = new Stop();
    stop.setCode("1109-b");
    stop.setDesc("stop description");

    stop.setId(stopId);
    stop.setLat(47.1);
    stop.setLocationType(0);
    stop.setLon(-122.1);
    stop.setName("stop name");
    stop.setParentStation(null);
    stop.setUrl("http://some/url");
    stop.setWheelchairBoarding(0);
    stop.setZoneId("stop zone");

    Mockito.when(_dao.getStopForId(stopId)).thenReturn(stop);

    StopNarrative.Builder narrative = StopNarrative.builder();
    narrative.setDirection("N");

    Mockito.when(_narrativeService.getStopForId(stopId)).thenReturn(
        narrative.create());

    AgencyAndId routeId = new AgencyAndId("1", "route");

    Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
    routeIds.add(routeId);

    Mockito.when(_routeService.getRouteCollectionIdsForStop(stopId)).thenReturn(
        routeIds);

    RouteBean.Builder routeBuilder = RouteBean.builder();
    routeBuilder.setId(AgencyAndIdLibrary.convertToString(routeId));
    RouteBean route = routeBuilder.create();
    Mockito.when(_routeBeanService.getRouteForId(routeId)).thenReturn(route);

    
    StopBean stopBean = _service.getStopForId(stopId);

    assertNotNull(stopBean);
    assertEquals(stop.getName(), stopBean.getName());
    assertEquals(stop.getLat(), stopBean.getLat(), 0.0);
    assertEquals(stop.getLon(), stopBean.getLon(), 0.0);
    assertEquals(stop.getCode(), stopBean.getCode());
    assertEquals(stop.getLocationType(), stopBean.getLocationType());

    List<RouteBean> routes = stopBean.getRoutes();
    assertEquals(1, routes.size());

    assertSame(route, routes.get(0));
  }
}
