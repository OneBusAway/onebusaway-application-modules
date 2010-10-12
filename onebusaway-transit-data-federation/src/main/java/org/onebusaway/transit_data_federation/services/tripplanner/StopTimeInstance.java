package org.onebusaway.transit_data_federation.services.tripplanner;

import java.text.DateFormat;
import java.util.Date;

import org.onebusaway.gtfs.model.AgencyAndId;

public class StopTimeInstance {

  private static final DateFormat DAY_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

  private static final DateFormat TIME_FORMAT = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private final BlockStopTimeEntry _stopTime;

  private final long _serviceDate;

  private AgencyAndId _vehicleId;

  public StopTimeInstance(BlockStopTimeEntry stopTime, Date serviceDate) {
    this(stopTime, serviceDate.getTime());
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate) {
    _stopTime = stopTime;
    _serviceDate = serviceDate;
  }

  public BlockStopTimeEntry getStopTime() {
    return _stopTime;
  }

  public BlockTripEntry getTrip() {
    return _stopTime.getTrip();
  }

  public int getSequence() {
    return _stopTime.getBlockSequence();
  }

  public StopEntry getStop() {
    return _stopTime.getStopTime().getStop();
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  public long getArrivalTime() {
    return _serviceDate + _stopTime.getStopTime().getArrivalTime() * 1000;
  }

  public long getDepartureTime() {
    return _serviceDate + _stopTime.getStopTime().getDepartureTime() * 1000;
  }

  public AgencyAndId getVehicleId() {
    return _vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    _vehicleId = vehicleId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeInstance))
      return false;
    StopTimeInstance other = (StopTimeInstance) obj;
    return _stopTime.equals(other._stopTime)
        && _serviceDate == other._serviceDate;
  }

  @Override
  public int hashCode() {
    return _stopTime.hashCode() + new Long(_serviceDate).hashCode();
  }

  @Override
  public String toString() {
    return "StopTimeInstance(stop=" + _stopTime.getStopTime().getStop().getId() + " trip=" + getTrip() + " service="
        + DAY_FORMAT.format(_serviceDate) + " arrival="
        + TIME_FORMAT.format(getArrivalTime()) + " departure="
        + TIME_FORMAT.format(getDepartureTime()) + ")";
  }
}
