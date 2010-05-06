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