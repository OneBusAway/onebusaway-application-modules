/**
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class ShapePointHelperTest {

  @Test
  public void test() {

    ShapePointHelper helper = new ShapePointHelper();

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    helper.setGtfsRelationalDao(gtfsDao);

    AgencyAndId shapeId = new AgencyAndId("1", "shapeA");

    ShapePoint p1 = point(47.652300128129454, -122.30622018270873);
    ShapePoint p2 = point(47.653181844549394, -122.30523312979125);
    ShapePoint p3 = point(47.654265901710744, -122.30511511259459);
    List<ShapePoint> points = Arrays.asList(p1, p2, p3);

    Mockito.when(gtfsDao.getShapePointsForShapeId(shapeId)).thenReturn(points);

    ShapePoints shapePoints = helper.getShapePointsForShapeId(shapeId);

    assertEquals(3, shapePoints.getSize());
    for (int i = 0; i < 3; i++) {
      assertEquals(points.get(i).getLat(), shapePoints.getLatForIndex(i), 1e-7);
      assertEquals(points.get(i).getLon(), shapePoints.getLonForIndex(i), 1e-7);
    }

    assertEquals(0.0, shapePoints.getDistTraveledForIndex(0), 0.01);
    assertEquals(122.79, shapePoints.getDistTraveledForIndex(1), 0.01);
    assertEquals(243.66, shapePoints.getDistTraveledForIndex(2), 0.01);

    helper.getShapePointsForShapeId(shapeId);

    // The second call should be cached
    Mockito.verify(gtfsDao, Mockito.times(1)).getShapePointsForShapeId(shapeId);
  }

  private ShapePoint point(double lat, double lon) {
    ShapePoint point = new ShapePoint();
    point.setLat(lat);
    point.setLon(lon);
    return point;
  }
}
