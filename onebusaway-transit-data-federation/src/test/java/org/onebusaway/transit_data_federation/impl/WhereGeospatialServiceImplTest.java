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
package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class WhereGeospatialServiceImplTest {

  @Test
  public void test() {
    WhereGeospatialServiceImpl service = new WhereGeospatialServiceImpl();

    TransitGraphDao dao = Mockito.mock(TransitGraphDao.class);
    service.setTransitGraphDao(dao);

    StopEntry stopA = stop("a", -0.5, -0.5);
    StopEntry stopB = stop("b", -0.5, 0.5);
    StopEntry stopC = stop("c", 0.5, -0.5);
    StopEntry stopD = stop("d", 0.5, 0.5);
    List<StopEntry> allStops = Arrays.asList(stopA, stopB, stopC, stopD);

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
}
