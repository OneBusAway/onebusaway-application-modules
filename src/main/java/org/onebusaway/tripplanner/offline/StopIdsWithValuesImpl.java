package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.model.StopIdsWithValues;

import java.io.Serializable;

/**
 * We favor slow add performance for faster read performance later on, since the
 * adding will be done once offline, but reading will be done frequently online.
 * 
 * @author bdferris
 */
class StopIdsWithValuesImpl implements StopIdsWithValues, Serializable {

  private static final long serialVersionUID = 1L;

  private String[] _stopIds = {};

  private int[] _values = {};

  public void setValue(String stopId, int value) {
    int index = findKey(stopId, true, value);
    _values[index] = value;
  }

  public void setMinValue(String stopId, int value) {
    int index = findKey(stopId, true, value);
    if (_values[index] > value)
      _values[index] = value;
  }

  /****
   * {@link StopIdsWithValues} Interface
   ****/

  public boolean isEmpty() {
    return _stopIds.length == 0;
  }

  public int size() {
    return _stopIds.length;
  }

  public String getStopId(int index) {
    return _stopIds[index];
  }

  public int getValue(int index) {
    return _values[index];
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private int findKey(String key, boolean create, int defaultValue) {

    for (int i = 0; i < _stopIds.length; i++) {
      if (_stopIds[i].equals(key))
        return i;
    }

    if (!create)
      return -1;

    if (_stopIds.length != _values.length)
      throw new IllegalStateException();

    int n = _stopIds.length;

    String[] stopIds = new String[n + 1];
    int[] values = new int[n + 1];

    System.arraycopy(_stopIds, 0, stopIds, 0, n);
    System.arraycopy(_values, 0, values, 0, n);

    stopIds[n] = key;
    values[n] = defaultValue;

    _stopIds = stopIds;
    _values = values;

    return n;
  }

}
