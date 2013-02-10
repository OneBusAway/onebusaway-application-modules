/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.EAccessibility;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyStopTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopEntryImpl implements StopEntry, Serializable {

  private static final long serialVersionUID = 2L;

  private final AgencyAndId _id;

  private final double _lat;

  private final double _lon;
  
  private EAccessibility _wheelchairBoarding = EAccessibility.UNKNOWN;

  private transient int _index;

  private transient List<BlockStopTimeIndex> _stopTimeIndices = null;

  private transient List<FrequencyBlockStopTimeIndex> _frequencyStopTimeIndices = null;

  private transient List<BlockStopSequenceIndex> _stopTripIndices = null;

  private transient List<FrequencyStopTripIndex> _frequencyStopTripIndices = null;

  public StopEntryImpl(AgencyAndId id, double lat, double lon) {
    if (id == null)
      throw new IllegalArgumentException("id must not be null");
    _id = id;
    _lat = lat;
    _lon = lon;
  }
  
  public void setWheelchairBoarding(EAccessibility wheelchairBoarding) {
    _wheelchairBoarding = wheelchairBoarding;
  }

  public void setIndex(int index) {
    _index = index;
  }

  public void addStopTimeIndex(BlockStopTimeIndex stopTimeIndex) {
    if (_stopTimeIndices == null)
      _stopTimeIndices = new ArrayList<BlockStopTimeIndex>();
    _stopTimeIndices.add(stopTimeIndex);
  }

  public List<BlockStopTimeIndex> getStopTimeIndices() {
    if (_stopTimeIndices == null)
      return Collections.emptyList();
    return _stopTimeIndices;
  }

  public void addFrequencyStopTimeIndex(
      FrequencyBlockStopTimeIndex stopTimeIndex) {
    if (_frequencyStopTimeIndices == null)
      _frequencyStopTimeIndices = new ArrayList<FrequencyBlockStopTimeIndex>();
    _frequencyStopTimeIndices.add(stopTimeIndex);
  }

  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndices() {
    if (_frequencyStopTimeIndices == null)
      return Collections.emptyList();
    return _frequencyStopTimeIndices;
  }

  public void addBlockStopTripIndex(BlockStopSequenceIndex index) {
    if (_stopTripIndices == null)
      _stopTripIndices = new ArrayList<BlockStopSequenceIndex>();
    _stopTripIndices.add(index);
  }

  public List<BlockStopSequenceIndex> getStopTripIndices() {
    if (_stopTripIndices == null)
      return Collections.emptyList();
    return _stopTripIndices;
  }

  public void addFrequencyStopTripIndex(FrequencyStopTripIndex index) {
    if (_frequencyStopTripIndices == null)
      _frequencyStopTripIndices = new ArrayList<FrequencyStopTripIndex>();
    _frequencyStopTripIndices.add(index);
  }

  public List<FrequencyStopTripIndex> getFrequencyStopTripIndices() {
    if (_frequencyStopTripIndices == null)
      return Collections.emptyList();
    return _frequencyStopTripIndices;
  }

  /****
   * {@link StopEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public double getStopLat() {
    return _lat;
  }

  @Override
  public double getStopLon() {
    return _lon;
  }

  @Override
  public CoordinatePoint getStopLocation() {
    return new CoordinatePoint(_lat, _lon);
  }
  
  @Override
  public EAccessibility getWheelchairBoarding() {
    return _wheelchairBoarding;
  }

  @Override
  public int getIndex() {
    return _index;
  }

  /****
   * {@link Object} Interface
   ****/

  /*
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopEntryImpl))
      return false;
    StopEntryImpl stop = (StopEntryImpl) obj;
    return _id.equals(stop.getId());
  }
  */

  /*
  @Override
  public int hashCode() {
    return _id.hashCode();
  }
  */

  @Override
  public String toString() {
    return "StopEntry(id=" + _id + ")";
  }

  /****
   * {@link Comparable} Interface
   ****/

  @Override
  public int compareTo(StopEntry o) {
    return this.getIndex() - o.getIndex();
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TransitGraphImpl.handleStopEntryRead(this);
  }

}
