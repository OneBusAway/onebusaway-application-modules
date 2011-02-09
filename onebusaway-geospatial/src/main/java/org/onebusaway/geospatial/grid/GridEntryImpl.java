package org.onebusaway.geospatial.grid;

public class GridEntryImpl<T> implements Grid.Entry<T> {

  private final GridIndex _index;
  private final T _value;

  public GridEntryImpl(GridIndex index, T value) {
    _index = index;
    _value = value;
  }

  public GridIndex getIndex() {
    return _index;
  }

  public T getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + ((_value == null) ? 0 : _value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof GridEntryImpl))
      return false;
    GridEntryImpl<?> other = (GridEntryImpl<?>) obj;
    if (_index == null) {
      if (other._index != null)
        return false;
    } else if (!_index.equals(other._index))
      return false;
    if (_value == null) {
      if (other._value != null)
        return false;
    } else if (!_value.equals(other._value))
      return false;
    return true;
  }
}
