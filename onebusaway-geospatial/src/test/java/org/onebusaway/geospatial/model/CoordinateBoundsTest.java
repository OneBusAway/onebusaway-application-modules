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

import static org.junit.Assert.*;

import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;

public class CoordinateBoundsTest {

  @Test
  public void testCoordinateBounds() {
    CoordinateBounds bounds = new CoordinateBounds();
    assertTrue(bounds.isEmpty());

    bounds.addPoint(1, 2);
    assertFalse(bounds.isEmpty());
    assertEquals(1, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(2, bounds.getMaxLon(), 0);
  }

  @Test
  public void testCoordinateBoundsDoubleDouble() {
    CoordinateBounds bounds = new CoordinateBounds(1, 2);
    assertFalse(bounds.isEmpty());
    assertEquals(1, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(2, bounds.getMaxLon(), 0);
  }

  @Test
  public void testCoordinateBoundsCoordinateBounds() {
    CoordinateBounds bounds = new CoordinateBounds(1, 2);
    bounds = new CoordinateBounds(bounds);
    assertFalse(bounds.isEmpty());
    assertEquals(1, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(2, bounds.getMaxLon(), 0);
  }

  @Test
  public void testCoordinateBoundsDoubleDoubleDoubleDouble() {
    CoordinateBounds bounds = new CoordinateBounds(1, 2,0,5);
    assertFalse(bounds.isEmpty());
    assertEquals(0, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(5, bounds.getMaxLon(), 0);
  }

  @Test
  public void testSetEmpty() {
    CoordinateBounds bounds = new CoordinateBounds(1,2);
    assertFalse(bounds.isEmpty());
    bounds.setEmpty(true);
    assertTrue(bounds.isEmpty());
    bounds.addPoint(3, 4);
    assertFalse(bounds.isEmpty());
    assertEquals(3, bounds.getMinLat(), 0);
    assertEquals(4, bounds.getMinLon(), 0);
    assertEquals(3, bounds.getMaxLat(), 0);
    assertEquals(4, bounds.getMaxLon(), 0);
  }

  @Test
  public void testGetSet() {
    CoordinateBounds bounds = new CoordinateBounds(1,2);
    bounds.setMinLat(3);
    bounds.setMinLon(4);
    bounds.setMaxLat(5);
    bounds.setMaxLon(6);
    assertEquals(3, bounds.getMinLat(), 0);
    assertEquals(4, bounds.getMinLon(), 0);
    assertEquals(5, bounds.getMaxLat(), 0);
    assertEquals(6, bounds.getMaxLon(), 0);
  }

  @Test
  public void testAddPoint() {
    CoordinateBounds bounds = new CoordinateBounds();
    bounds.addPoint(1,2);
    assertEquals(1, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(2, bounds.getMaxLon(), 0);
    bounds.addPoint(0,5);
    assertEquals(0, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(1, bounds.getMaxLat(), 0);
    assertEquals(5, bounds.getMaxLon(), 0);
  }

  @Test
  public void testAddBounds() {
    CoordinateBounds bounds = new CoordinateBounds();
    bounds.addBounds(new CoordinateBounds(1,2,3,4));
    assertEquals(1, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(3, bounds.getMaxLat(), 0);
    assertEquals(4, bounds.getMaxLon(), 0);
    bounds.addBounds(new CoordinateBounds(0,3,2,5));
    assertEquals(0, bounds.getMinLat(), 0);
    assertEquals(2, bounds.getMinLon(), 0);
    assertEquals(3, bounds.getMaxLat(), 0);
    assertEquals(5, bounds.getMaxLon(), 0);
  }

  @Test
  public void testContains() {
    CoordinateBounds bounds = new CoordinateBounds(1,2,3,4);
    assertTrue(bounds.contains(1, 2));
    assertTrue(bounds.contains(3, 4));
    assertTrue(bounds.contains(2, 3));
    assertFalse(bounds.contains(0, 3));
    assertFalse(bounds.contains(2, 0));
  }

  @Test
  public void testIntersects() {
    CoordinateBounds bounds = new CoordinateBounds(1,2,3,4);
    assertTrue(bounds.intersects(new CoordinateBounds(1,2,3,4)));
    assertTrue(bounds.intersects(new CoordinateBounds(0,0,2,3)));
    assertTrue(bounds.intersects(new CoordinateBounds(0,0,1,2)));
    assertFalse(bounds.intersects(new CoordinateBounds(0,0,1,1)));
  }
}
