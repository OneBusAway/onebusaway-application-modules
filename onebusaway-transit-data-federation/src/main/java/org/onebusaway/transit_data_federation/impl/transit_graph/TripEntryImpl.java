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
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class TripEntryImpl implements TripEntry, Serializable {

  private static final long serialVersionUID = 6L;

  private AgencyAndId _id;

  private RouteEntryImpl _route;

  private String _directionId;

  private BlockEntryImpl _block;

  private LocalizedServiceId _serviceId;

  private AgencyAndId _shapeId;

  private List<StopTimeEntry> _stopTimes;

  private double _totalTripDistance;

  private FrequencyEntry _frequencyLabel;

  public TripEntryImpl setId(AgencyAndId id) {
    _id = id;
    return this;
  }

  public TripEntryImpl setRoute(RouteEntryImpl route) {
    _route = route;
    return this;
  }

  public TripEntryImpl setDirectionId(String directionId) {
    _directionId = directionId;
    return this;
  }

  public TripEntryImpl setBlock(BlockEntryImpl block) {
    _block = block;
    return this;
  }

  public TripEntryImpl setServiceId(LocalizedServiceId serviceId) {
    _serviceId = serviceId;
    return this;
  }

  public void setShapeId(AgencyAndId shapeId) {
    _shapeId = shapeId;
  }

  public void setStopTimes(List<StopTimeEntry> stopTimes) {
    _stopTimes = stopTimes;
  }

  public void setTotalTripDistance(double totalTripDistance) {
    _totalTripDistance = totalTripDistance;
  }

  public void setFrequencyLabel(FrequencyEntry frequencyLabel) {
    _frequencyLabel = frequencyLabel;
  }

  /****
   * {@link TripEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public RouteEntry getRoute() {
    return _route;
  }

  @Override
  public RouteCollectionEntry getRouteCollection() {
    return _route.getParent();
  }

  @Override
  public String getDirectionId() {
    return _directionId;
  }

  @Override
  public BlockEntryImpl getBlock() {
    return _block;
  }

  @Override
  public LocalizedServiceId getServiceId() {
    return _serviceId;
  }

  @Override
  public AgencyAndId getShapeId() {
    return _shapeId;
  }

  @Override
  public List<StopTimeEntry> getStopTimes() {
    return _stopTimes;
  }

  @Override
  public double getTotalTripDistance() {
    return _totalTripDistance;
  }

  @Override
  public FrequencyEntry getFrequencyLabel() {
    return _frequencyLabel;
  }

  @Override
  public String toString() {
    return "Trip(" + _id + ")";
  }

  /****
   * Serialization
   ****/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TransitGraphImpl.handleTripEntryRead(this);
  }
}
