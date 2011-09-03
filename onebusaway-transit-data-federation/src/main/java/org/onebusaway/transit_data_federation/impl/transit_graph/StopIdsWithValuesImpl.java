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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * We favor slow add performance for faster read performance later on, since the
 * adding will be done once offline, but reading will be done frequently online.
 * 
 * @author bdferris
 */
class StopIdsWithValuesImpl implements StopEntriesWithValues, Serializable {

  private static final long serialVersionUID = 1L;

  private StopEntry[] _stops = {};

  private int[] _values = {};

  public void setValue(StopEntry entry, int value) {
    int index = findKey(entry, true, value);
    _values[index] = value;
  }

  public void setMinValue(StopEntry entry, int value) {
    int index = findKey(entry, true, value);
    if (_values[index] > value)
      _values[index] = value;
  }

  /****
   * {@link StopEntriesWithValues} Interface
   ****/

  public boolean isEmpty() {
    return _stops.length == 0;
  }

  public int size() {
    return _stops.length;
  }

  public StopEntry getStopEntry(int index) {
    return _stops[index];
  }

  public int getValue(int index) {
    return _values[index];
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private int findKey(StopEntry key, boolean create, int defaultValue) {

    for (int i = 0; i < _stops.length; i++) {
      if (_stops[i].equals(key))
        return i;
    }

    if (!create)
      return -1;

    if (_stops.length != _values.length)
      throw new IllegalStateException();

    int n = _stops.length;

    StopEntry[] stops = new StopEntry[n + 1];
    int[] values = new int[n + 1];

    System.arraycopy(_stops, 0, stops, 0, n);
    System.arraycopy(_values, 0, values, 0, n);

    stops[n] = key;
    values[n] = defaultValue;

    _stops = stops;
    _values = values;

    return n;
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  private void writeObject(ObjectOutputStream out) throws IOException {
    AgencyAndId[] ids = new AgencyAndId[_stops.length];
    for (int i = 0; i < ids.length; i++) {
      StopEntry entry = _stops[i];
      ids[i] = entry.getId();
    }
    out.writeObject(ids);
    out.writeObject(_values);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

    AgencyAndId[] ids = (AgencyAndId[]) in.readObject();
    _values = (int[]) in.readObject();

    _stops = new StopEntry[ids.length];

    for (int i = 0; i < ids.length; i++)
      TransitGraphImpl.addStopEntryCallback(ids[i], new StopEntryCallback(i));
  }

  private class StopEntryCallback implements EntryCallback<StopEntryImpl> {

    private int _index;

    public StopEntryCallback(int index) {
      _index = index;
    }

    public void handle(StopEntryImpl entry) {
      _stops[_index] = entry;
    }
  }

}
