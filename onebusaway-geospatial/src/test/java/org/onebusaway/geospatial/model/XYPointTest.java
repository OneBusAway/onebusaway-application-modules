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
package org.onebusaway.geospatial.model;

import junit.framework.TestCase;

public class XYPointTest extends TestCase {

  private static final double[] P2 = new double[] {1.0, 2.0};

  public void testXYPointDoubleDouble() {

    XYPoint p = new XYPoint(1, 2);

    assertEquals(2, p.getDimensions());
    assertEquals(1.0, p.getX(), 0.0);
    assertEquals(2.0, p.getY(), 0.0);

  }

  public void testGetOrdinate() {

    XYPoint p = new XYPoint(P2);

    assertEquals(1.0, p.getOrdinate(0), 0.0);
    assertEquals(2.0, p.getOrdinate(1), 0.0);
  }

  public void testGetDistanceP() {

    XYPoint p1 = new XYPoint(1.0, 2.0);
    XYPoint p2 = new XYPoint(4.0, 6.0);

    assertEquals(5.0, p1.getDistance(p2), 0.0);
  }

  public void testTranslateDoubleArray() {

    XYPoint p = new XYPoint(P2);
    XYPoint p2 = p.translate(P2);

    assertEquals(1.0, p.getX(), 0.0);
    assertEquals(2.0, p.getY(), 0.0);

    assertEquals(2.0, p2.getX(), 0.0);
    assertEquals(4.0, p2.getY(), 0.0);
  }
}
