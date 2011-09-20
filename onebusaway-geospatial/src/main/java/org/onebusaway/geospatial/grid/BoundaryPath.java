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

import java.util.ArrayList;
import java.util.List;

public class BoundaryPath {

  private List<GridIndex> _indices = new ArrayList<GridIndex>();

  private List<EDirection> _directions = new ArrayList<EDirection>();

  public boolean isEmpty() {
    return _indices.isEmpty();
  }

  public int size() {
    return _indices.size();
  }

  public void addEdge(GridIndex index, EDirection direction) {
    _indices.add(index);
    _directions.add(direction);
  }

  public List<GridIndex> getIndices() {
    return _indices;
  }

  public GridIndex getIndex(int index) {
    return _indices.get(index);
  }

  public List<EDirection> getDirections() {
    return _directions;
  }

  public EDirection getDirection(int index) {
    return _directions.get(index);
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      GridIndex index = _indices.get(i);
      EDirection direction = _directions.get(i);
      if (i > 0)
        b.append('|');
      b.append(index.getX());
      b.append(',');
      b.append(index.getY());
      b.append(',');
      b.append(getDirectionAsString(direction));
    }
    return b.toString();
  }

  private String getDirectionAsString(EDirection direction) {
    switch (direction) {
      case UP:
        return "U";
      case RIGHT:
        return "R";
      case DOWN:
        return "D";
      case LEFT:
        return "L";
      default:
        throw new IllegalStateException();
    }
  }
}
