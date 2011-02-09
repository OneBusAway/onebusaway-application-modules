package org.onebusaway.geospatial.grid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapGrid<T> extends AbstractGrid<T> {

  private Map<GridIndex, T> _elements = new HashMap<GridIndex, T>();

  public boolean contains(int x, int y) {
    return _elements.containsKey(new GridIndex(x, y));
  }

  public T get(int x, int y) {
    return _elements.get(new GridIndex(x, y));
  }

  public void set(int x, int y, T element) {
    _elements.put(new GridIndex(x, y), element);
  }

  public Iterable<Grid.Entry<T>> getEntries() {
    return new Iterable<Grid.Entry<T>>() {
      public Iterator<Grid.Entry<T>> iterator() {
        return new IteratorImpl<T>(_elements.entrySet().iterator());
      }
    };
  }

  private static class IteratorImpl<T> implements Iterator<Grid.Entry<T>> {

    private Iterator<Map.Entry<GridIndex, T>> _it;

    public IteratorImpl(Iterator<Map.Entry<GridIndex, T>> iterator) {
      _it = iterator;
    }

    public boolean hasNext() {
      return _it.hasNext();
    }

    public Grid.Entry<T> next() {
      Map.Entry<GridIndex, T> next = _it.next();
      return new GridEntryImpl<T>(next.getKey(), next.getValue());
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
