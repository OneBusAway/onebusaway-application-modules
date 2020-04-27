/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;

public class NearbyStopsBeanServiceImplTest {

  private NearbyStopsBeanServiceImpl _service;

  private GeospatialBeanService _geoBeanService;

  @Before
  public void setup() {
    _service = new NearbyStopsBeanServiceImpl();

    _geoBeanService = Mockito.mock(GeospatialBeanService.class);
    _service.setGeospatialBeanService(_geoBeanService);
  }

  @Test
  public void test() {

    AgencyAndId stopIdA = new AgencyAndId("1", "stopA");
    AgencyAndId stopIdB = new AgencyAndId("1", "stopB");

    StopBean stop = new StopBean();
    stop.setId(AgencyAndIdLibrary.convertToString(stopIdA));
    stop.setLat(47.0);
    stop.setLon(-122.0);

    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    stopIds.add(stopIdA);
    stopIds.add(stopIdB);

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(stop.getLat(),
        stop.getLon(), 400);
    Mockito.when(_geoBeanService.getStopsByBounds(bounds)).thenReturn(stopIds);

    List<AgencyAndId> nearby = _service.getNearbyStops(stop, 400);
    assertEquals(1, nearby.size());
    assertTrue(nearby.contains(stopIdB));
  }
}
