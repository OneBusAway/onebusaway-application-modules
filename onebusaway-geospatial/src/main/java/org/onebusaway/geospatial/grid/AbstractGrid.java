package org.onebusaway.geospatial.grid;

public abstract class AbstractGrid<T> implements Grid<T> {

  public boolean contains(GridIndex index) {
    return contains(index.getX(), index.getY());
  }

  public T get(GridIndex index) {
    return get(index.getX(), index.getY());
  }

  public void set(GridIndex index, T element) {
    set(index.getX(), index.getY(), element);
  }
}
