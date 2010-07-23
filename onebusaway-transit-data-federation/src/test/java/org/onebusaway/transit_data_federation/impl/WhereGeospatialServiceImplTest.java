package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class WhereGeospatialServiceImplTest {

  @Test
  public void test() {
    WhereGeospatialServiceImpl service = new WhereGeospatialServiceImpl();

    GtfsRelationalDao dao = Mockito.mock(GtfsRelationalDao.class);
    service.setGtfsRelationalDao(dao);

    Stop stopA = stop("a", -0.5, -0.5);
    Stop stopB = stop("b", -0.5, 0.5);
    Stop stopC = stop("c", 0.5, -0.5);
    Stop stopD = stop("d", 0.5, 0.5);
    Collection<Stop> allStops = Arrays.asList(stopA, stopB, stopC, stopD);

    Mockito.when(dao.getAllStops()).thenReturn(allStops);

    service.initialize();

    List<AgencyAndId> stops = service.getStopsByBounds(new CoordinateBounds(-1,
        -1, 0, 0));
    assertEquals(1, stops.size());
    assertTrue(stops.contains(stopA.getId()));

    stops = service.getStopsByBounds(new CoordinateBounds(0, -1, 1, 0));
    assertEquals(1, stops.size());
    assertTrue(stops.contains(stopC.getId()));

    stops = service.getStopsByBounds(new CoordinateBounds(-1, -1, 1, 0));
    assertEquals(2, stops.size());
    assertTrue(stops.contains(stopA.getId()));
    assertTrue(stops.contains(stopC.getId()));

    stops = service.getStopsByBounds(new CoordinateBounds(-1, -1, 1, 1));
    assertEquals(4, stops.size());
    assertTrue(stops.contains(stopA.getId()));
    assertTrue(stops.contains(stopB.getId()));
    assertTrue(stops.contains(stopC.getId()));
    assertTrue(stops.contains(stopD.getId()));

    stops = service.getStopsByBounds(new CoordinateBounds(0.8, 0.8, 1, 1));
    assertEquals(0, stops.size());
  }

  private Stop stop(String id, double lat, double lon) {
    Stop stop = new Stop();
    stop.setId(id(id));
    stop.setLat(lat);
    stop.setLon(lon);
    return stop;
  }

  private AgencyAndId id(String id) {
    return new AgencyAndId("agency", id);
  }
}
