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
package org.onebusaway.geospatial.grid;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapGridTest extends TestCase {

  public void testGrid() {
    for (int i = 1; i < 10; i++) {
      for (int j = 1; j < 10; j++) {
        testSimpleNumericGrid(new MapGrid<Integer>(), i, j);
        testRandomNumericGrid(new MapGrid<Integer>(), i, j);
      }
    }
  }

  private void testSimpleNumericGrid(Grid<Integer> grid, int width, int height) {

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        assertFalse(grid.contains(x, y));
        grid.set(x, y, x * height + y);
        assertTrue(grid.contains(x, y));
        assertEquals(x * height + y, grid.get(x, y).intValue());
      }
    }

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        assertEquals(x * height + y, grid.get(x, y).intValue());
      }
    }
  }

  private void testRandomNumericGrid(Grid<Integer> grid, int width, int height) {
    List<Integer> indices = new ArrayList<Integer>();
    for (int x = 0; x < width * height; x++)
      indices.add(x);
    Collections.shuffle(indices);
    for (int index : indices) {
      int x = index % width;
      int y = index / width;
      assertFalse(grid.contains(x, y));
      grid.set(x, y, index);
      assertTrue(grid.contains(x, y));
      assertEquals(index, grid.get(x, y).intValue());
    }

  }
}
