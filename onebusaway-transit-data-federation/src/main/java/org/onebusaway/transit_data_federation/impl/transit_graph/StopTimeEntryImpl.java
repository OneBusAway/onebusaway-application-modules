package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class StopTimeEntryImpl implements StopTimeEntry, Serializable {

  private static final long serialVersionUID = 3L;

  private int _stopTimeId;
  private int _arrivalTime;
  private int _departureTime;
  private int _sequence;
  private int _dropOffType;
  private int _pickupType;
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

  private void writeObject(ObjectOutputStream out) throws IOException {

    out.writeInt(_stopTimeId);
    out.writeInt(_arrivalTime);
    out.writeInt(_departureTime);
    out.writeInt(_sequence);
    out.writeInt(_dropOffType);
    out.writeInt(_pickupType);
    out.writeDouble(_shapeDistTraveled);
    out.writeInt(_accumulatedSlackTime);

    out.writeObject(_trip.getId());
    out.writeObject(_stop.getId());
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {

    _stopTimeId = in.readInt();
    _arrivalTime = in.readInt();
    _departureTime = in.readInt();
    _sequence = in.readInt();
    _dropOffType = in.readInt();
    _pickupType = in.readInt();
    _shapeDistTraveled = in.readDouble();
    _accumulatedSlackTime = in.readInt();

    AgencyAndId tripId = (AgencyAndId) in.readObject();
    AgencyAndId stopId = (AgencyAndId) in.readObject();

    TransitGraphImpl.addTripEntryCallback(tripId,
        new EntryCallback<TripEntryImpl>() {
          public void handle(TripEntryImpl entry) {
            _trip = entry;
          }
        });

    TransitGraphImpl.addStopEntryCallback(stopId,
        new EntryCallback<StopEntryImpl>() {
          public void handle(StopEntryImpl entry) {
            _stop = entry;
          }
        });
  }

}
