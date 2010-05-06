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

    private static final long serialVersionUID = 1L;

    public boolean isEnabled(Grid.Entry<T> entry) {
      return _filter.isEnabled(entry.getValue());
    }
  }

}
