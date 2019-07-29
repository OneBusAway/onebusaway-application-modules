/**
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
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

import java.io.Serializable;
import java.util.List;

import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class StopTimeEntryImpl implements StopTimeEntry, Serializable {

  private static final long serialVersionUID = 8L;

  private int _stopTimeId;
  private int _arrivalTime;
  private int _departureTime;
  private OccupancyStatus _historicalOccupancy;
  private int _sequence;
  private int _gtfsSequence;
  private int _dropOffType;
  private int _pickupType;
  private int _shapePointIndex = -1;
  private double _shapeDistTraveled = Double.NaN;
  private int _accumulatedSlackTime = 0;
  private int _totalStopsInTrip;

  private StopEntryImpl _stop;

  private TripEntryImpl _trip;

  public void setId(int id) {
    _stopTimeId = id;
  }

  public void setArrivalTime(int arrivalTime) {
    _arrivalTime = arrivalTime;
  }

  public void setDepartureTime(int departureTime) {
    _departureTime = departureTime;
  }

  public void setHistoricalOccupancy(OccupancyStatus historicalOccupancy) {_historicalOccupancy = historicalOccupancy; }
  
  public StopTimeEntryImpl setTime(int time) {
    _arrivalTime = time;
    _departureTime = time;
    return this;
  }

  public void setSequence(int sequence) {
    _sequence = sequence;
  }

  public void setGtfsSequence(int gtfsSequence) {
    _gtfsSequence = gtfsSequence;
  }

  public void setDropOffType(int dropOffType) {
    _dropOffType = dropOffType;
  }

  public void setPickupType(int pickupType) {
    _pickupType = pickupType;
  }

  public StopTimeEntryImpl setStop(StopEntryImpl stop) {
    _stop = stop;
    return this;
  }

  public void setTrip(TripEntryImpl trip) {
    _trip = trip;
  }

  public void setShapePointIndex(int shapePointIndex) {
    _shapePointIndex = shapePointIndex;
  }

  public boolean isShapeDistTraveledSet() {
    return !Double.isNaN(_shapeDistTraveled);
  }

  public void setShapeDistTraveled(double shapeDistTraveled) {
    _shapeDistTraveled = shapeDistTraveled;
  }

  public void setAccumulatedSlackTime(int accumulatedSlackTime) {
    _accumulatedSlackTime = accumulatedSlackTime;
  }
  
  public void setTotalStopsInTrip(int totalStopsInTrip) {
    _totalStopsInTrip = totalStopsInTrip;
  }

  /****
   * {@link StopTimeEntry} Interface
   ****/

  @Override
  public int getId() {
    return _stopTimeId;
  }

  @Override
  public int getArrivalTime() {
    return _arrivalTime;
  }

  @Override
  public int getDepartureTime() {
    return _departureTime;
  }

  @Override
  public OccupancyStatus getHistoricalOccupancy() { return _historicalOccupancy; }

  @Override
  public int getSequence() {
    return _sequence;
  }

  @Override
  public int getGtfsSequence() {
    return _gtfsSequence;
  }

  @Override
  public int getDropOffType() {
    return _dropOffType;
  }

  @Override
  public int getPickupType() {
    return _pickupType;
  }

  @Override
  public StopEntryImpl getStop() {
    return _stop;
  }

  @Override
  public TripEntryImpl getTrip() {
    return _trip;
  }

  @Override
  public int getShapePointIndex() {
    return _shapePointIndex;
  }

  @Override
  public double getShapeDistTraveled() {
    return _shapeDistTraveled;
  }

  @Override
  public int getSlackTime() {
    return _departureTime - _arrivalTime;
  }

  @Override
  public int getAccumulatedSlackTime() {
    return _accumulatedSlackTime;
  }

  @Override
  public int getTotalStopsInTrip() {
    return _totalStopsInTrip;
  }
  
  @Override
  public String toString() {
    return "StopTimeEntryImpl(stop=" + _stop.getId() + " trip=" + _trip
        + " arrival=" + _arrivalTime + " departure=" + _departureTime + ")";
  }
}
