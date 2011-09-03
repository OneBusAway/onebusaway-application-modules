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
