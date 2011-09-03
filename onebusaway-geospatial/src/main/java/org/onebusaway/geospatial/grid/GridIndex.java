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
/**
 * 
 */
package org.onebusaway.geospatial.grid;

public class GridIndex {

  private int _x;

  private int _y;

  public GridIndex(int x, int y) {
    _x = x;
    _y = y;
  }

  public int getX() {
    return _x;
  }

  public int getY() {
    return _y;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _x;
    result = prime * result + _y;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof GridIndex))
      return false;
    GridIndex other = (GridIndex) obj;
    if (_x != other._x)
      return false;
    if (_y != other._y)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "[" + _x + "," + _y + "]";
  }
}