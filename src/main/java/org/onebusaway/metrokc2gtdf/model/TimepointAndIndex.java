/**
 * 
 */
package org.onebusaway.metrokc2gtdf.model;

public final class TimepointAndIndex {

  private int _timepoint;

  private int _index;

  public TimepointAndIndex(int timepoint, int index) {
    _timepoint = timepoint;
    _index = index;
  }

  public int getTimepoint() {
    return _timepoint;
  }

  public int getIndex() {
    return _index;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TimepointAndIndex))
      return false;
    TimepointAndIndex id = (TimepointAndIndex) obj;
    return _timepoint == id._timepoint && _index == id._index;
  }

  @Override
  public int hashCode() {
    return 7 * _timepoint + 13 * _index;
  }
}