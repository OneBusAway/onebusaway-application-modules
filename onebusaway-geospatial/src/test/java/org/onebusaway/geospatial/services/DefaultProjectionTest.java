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
package org.onebusaway.geospatial.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;

public class DefaultProjectionTest {

  private static Proj4Projection _projection = new Proj4Projection(
      "+init=nad83:4601", "+units=us-ft");

  @Test
  public void testProjectionUW() throws NumberFormatException, IOException {
    assertPoint(47.6685794512116, -122.288293317482, 1281949.4999999,
        247265.91999397);
    assertPoint(47.65458077, -122.30502529, 1277729.9998584972,
        242239.0006173002);
  }

  private void assertPoint(double lat, double lon, double x, double y) {
    CoordinatePoint point = new CoordinatePoint(lat, lon);
    XYPoint p = _projection.forward(point);

    assertEquals(x, p.getX(), 0.01);
    assertEquals(y, p.getY(), 0.01);
  }

}
