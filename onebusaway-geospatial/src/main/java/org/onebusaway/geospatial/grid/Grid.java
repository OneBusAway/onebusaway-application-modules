package org.onebusaway.geospatial.grid;

public interface Grid<T> {

  public void set(int x, int y, T element);

  public void set(GridIndex index, T element);

  public boolean contains(int x, int y);

  public boolean contains(GridIndex index);

  public T get(int x, int y);

  public T get(GridIndex index);

  public Iterable<Entry<T>> getEntries();

  public interface Entry<T> {
    public GridIndex getIndex();

    public T getValue();
  }
}
