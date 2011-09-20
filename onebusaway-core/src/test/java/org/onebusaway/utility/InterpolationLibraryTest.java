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
package org.onebusaway.utility;

import static org.onebusaway.utility.InterpolationLibrary.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

public class InterpolationLibraryTest {

  @Test
  public void testSimple() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(-1.5, interpolate(values, -0.75), 0.0);
    assertEquals(-1.0, interpolate(values, -0.5), 0.0);
    assertEquals(-0.5, interpolate(values, -0.25), 0.0);

    assertEquals(0.0, interpolate(values, 0.0), 0.0);

    assertEquals(0.5, interpolate(values, 0.25), 0.0);
    assertEquals(1.0, interpolate(values, 0.5), 0.0);
    assertEquals(1.5, interpolate(values, 0.75), 0.0);

    assertEquals(2.0, interpolate(values, 1.0), 0.0);

    assertEquals(3.0, interpolate(values, 1.25), 0.0);
    assertEquals(4.0, interpolate(values, 1.5), 0.0);
    assertEquals(5.0, interpolate(values, 1.75), 0.0);

    assertEquals(6.0, interpolate(values, 2.0), 0.0);

    assertEquals(7.0, interpolate(values, 2.25), 0.0);
    assertEquals(8.0, interpolate(values, 2.5), 0.0);
    assertEquals(9.0, interpolate(values, 2.75), 0.0);
  }

  @Test
  public void testExceptionOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    try {
      interpolate(values, -0.25, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

    assertEquals(0.0, interpolate(values, 0.0), 0.0);
    assertEquals(4.0, interpolate(values, 1.5), 0.0);
    assertEquals(6.0, interpolate(values, 2.0), 0.0);

    try {
      interpolate(values, 2.25, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }
  }

  @Test
  public void testLastValueOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(0.0,
        interpolate(values, -0.25, EOutOfRangeStrategy.LAST_VALUE), 0.0);
    assertEquals(0.0, interpolate(values, 0.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(4.0, interpolate(values, 1.5, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0, interpolate(values, 2.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0,
        interpolate(values, 2.25, EOutOfRangeStrategy.LAST_VALUE), 0.0);
  }

  @Test
  public void testInterpolateOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(-1.5,
        interpolate(values, -0.75, EOutOfRangeStrategy.INTERPOLATE), 0.0);
    assertEquals(0.0, interpolate(values, 0.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(4.0, interpolate(values, 1.5, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0, interpolate(values, 2.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(7.0,
        interpolate(values, 2.25, EOutOfRangeStrategy.INTERPOLATE), 0.0);
  }

  @Test
  public void interpolateArray() {

    double[] x = {0, 2, 5, 6};
    double[] y = {4, 1, 3, 2};

    assertEquals(4.0, interpolate(x, y, 0, EOutOfRangeStrategy.INTERPOLATE),
        0.0);
    assertEquals(2.5, interpolate(x, y, 1.0, EOutOfRangeStrategy.INTERPOLATE),
        0.0);
    assertEquals(1.0, interpolate(x, y, 2.0, EOutOfRangeStrategy.INTERPOLATE),
        0.0);
    assertEquals(1.0 + 2.0 / 3,
        interpolate(x, y, 3.0, EOutOfRangeStrategy.INTERPOLATE), 0.0);
    assertEquals(2.75,
        interpolate(x, y, 5.25, EOutOfRangeStrategy.INTERPOLATE), 0.0);
    assertEquals(2, interpolate(x, y, 6.0, EOutOfRangeStrategy.INTERPOLATE),
        0.0);

    assertEquals(5.5, interpolate(x, y, -1, EOutOfRangeStrategy.INTERPOLATE),
        0.0);
    assertEquals(4.0, interpolate(x, y, -1, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    try {
      interpolate(x, y, -1, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

    assertEquals(1.0, interpolate(x, y, 7, EOutOfRangeStrategy.INTERPOLATE),
        0.0);
    assertEquals(2.0, interpolate(x, y, 7, EOutOfRangeStrategy.LAST_VALUE), 0.0);
    try {
      interpolate(x, y, 7, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

  }

  @Test
  public void testInterpolatePair() {
    assertEquals(10.5, interpolatePair(0.0, 10.0, 4.0, 8.0, -1.0), 0.0);
    assertEquals(10.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 0.0), 0.0);
    assertEquals(9.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 1.0), 0.0);
    assertEquals(9.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 2.0), 0.0);
    assertEquals(8.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 3.0), 0.0);
    assertEquals(8.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 4.0), 0.0);
    assertEquals(7.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 5.0), 0.0);

  }
}
