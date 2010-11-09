package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class StopTimeEntryImpl implements StopTimeEntry, Serializable {

  private static final long serialVersionUID = 5L;

  private int _stopTimeId;
  private int _arrivalTime;
  private int _departureTime;
  private int _sequence;
  private int _dropOffType;
  private int _pickupType;
  private int _shapePointIndex = -1;
  private double _shapeDistTraveled = Double.NaN;
  private int _accumulatedSlackTime = 0;

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

  public void setSequence(int sequence) {
    _sequence = sequence;
  }

  public void setDropOffType(int dropOffType) {
    _dropOffType = dropOffType;
  }

  public void setPickupType(int pickupType) {
    _pickupType = pickupType;
  }

  public void setStop(StopEntryImpl stop) {
    _stop = stop;
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
  public int getSequence() {
    return _sequence;
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
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeEntryImpl))
      return false;
    StopTimeEntryImpl other = (StopTimeEntryImpl) obj;
    return _stopTimeId == other._stopTimeId;
  }

  @Override
  public int hashCode() {
    return _stopTimeId;
  }

  @Override
  public String toString() {
    return "StopTimeEntryImpl(stop=" + _stop.getId() + " trip=" + _trip
        + " arrival=" + _arrivalTime + " departure=" + _departureTime + ")";
  }
}
