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

import org.onebusaway.utility.filter.FilteredIterable;
import org.onebusaway.utility.filter.IFilter;

public class FilteredGrid<T> extends AbstractGrid<T> {

  private Grid<T> _grid;
  private IFilter<T> _filter;

  public FilteredGrid(Grid<T> grid, IFilter<T> filter) {
    _grid = grid;
    _filter = filter;
  }

  public boolean contains(int x, int y) {
    return get(x, y) != null;
  }

  public T get(int x, int y) {
    T element = _grid.get(x, y);
    if (element != null && _filter.isEnabled(element))
      return element;
    return null;
  }

  public Iterable<Grid.Entry<T>> getEntries() {
    return new FilteredIterable<Grid.Entry<T>>(_grid.getEntries(),
        new EntryFilter());
  }

  public void set(int x, int y, T element) {
    if (_filter.isEnabled(element))
      _grid.set(x, y, element);
  }

  private class EntryFilter implements IFilter<Grid.Entry<T>> {

    public boolean isEnabled(Grid.Entry<T> entry) {
      return _filter.isEnabled(entry.getValue());
    }
  }

}
