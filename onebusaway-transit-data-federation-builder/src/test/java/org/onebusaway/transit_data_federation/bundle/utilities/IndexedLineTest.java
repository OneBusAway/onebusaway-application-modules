/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.transit_data_federation.bundle.utilities;

import org.onebusaway.geospatial.model.CoordinatePoint;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class IndexedLineTest {

  @Test
  public void testIndexedLine() {
    IndexedLine il = new IndexedLine();

    il.addPoint(10, 0, new CoordinatePoint(40.76430333537471, -73.97300844878706));
    il.addPoint(20, 250.36, new CoordinatePoint(40.7639649335897, -73.97222185573889));
    il.addPoint(30, 512.90, new CoordinatePoint(40.76359440983835, -73.97140902773853));

    assertEquals(10, il.interpolateIndex(100));
    assertEquals(20, il.interpolateIndex(245));
    assertEquals(20, il.interpolateIndex(255));
    assertEquals(30, il.interpolateIndex(500));

    assertEquals(38.1, il.interpolateDistance(0, 125), 0.1);
    assertEquals(114.3, il.interpolateDistance(0, 375), 0.5);
    assertEquals(194.2, il.interpolateDistance(0, 637), 0.5);

    assertEquals(156.1, il.interpolateDistance(125, 637), 0.5);
  }
}
