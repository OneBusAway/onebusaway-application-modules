package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.container.cache.CacheableKey;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@CacheableKey(keyFactory = StopEntryImplKeyFactoryImpl.class)
public class StopTimeEntryImpl implements StopTimeEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private int _stopTimeId;
  private int _arrivalTime;
  private int _departureTime;
  private int _sequence;
  private int _dropOffType;
  private int _pickupType;

  private StopEntryImpl _stop;

  private TripEntryImpl _trip;

  public int getId() {
    return _stopTimeId;
  }

  public void setId(int id) {
    _stopTimeId = id;
  }

  public int getArrivalTime() {
    return _arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    _arrivalTime = arrivalTime;
  }

  public int getDepartureTime() {
    return _departureTime;
  }

  public void setDepartureTime(int departureTime) {
    _departureTime = departureTime;
  }

  public int getSequence() {
    return _sequence;
  }

  public void setSequence(int sequence) {
    _sequence = sequence;
  }

  public int getDropOffType() {
    return _dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    _dropOffType = dropOffType;
  }

  public int getPickupType() {
    return _pickupType;
  }

  public void setPickupType(int pickupType) {
    _pickupType = pickupType;
  }

  public StopEntryImpl getStop() {
    return _stop;
  }

  public void setStop(StopEntryImpl stop) {
    _stop = stop;
  }

  public TripEntryImpl getTrip() {
    return _trip;
  }

  public void setTrip(TripEntryImpl trip) {
    _trip = trip;
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
    return "StopTimeProxyImpl(trip=" + _trip + " arrival=" + _arrivalTime
        + " departure=" + _departureTime + ")";
  }

  private void writeObject(ObjectOutputStream out) throws IOException {

    out.writeInt(_stopTimeId);
    out.writeInt(_arrivalTime);
    out.writeInt(_departureTime);
    out.writeInt(_sequence);
    out.writeInt(_dropOffType);
    out.writeInt(_pickupType);

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

    AgencyAndId tripId = (AgencyAndId) in.readObject();
    AgencyAndId stopId = (AgencyAndId) in.readObject();

    TripPlannerGraphImpl.addTripEntryCallback(tripId,
        new EntryCallback<TripEntryImpl>() {
          public void handle(TripEntryImpl entry) {
            _trip = entry;
          }
        });

    TripPlannerGraphImpl.addStopEntryCallback(stopId,
        new EntryCallback<StopEntryImpl>() {
          public void handle(StopEntryImpl entry) {
            _stop = entry;
          }
        });
  }

}
