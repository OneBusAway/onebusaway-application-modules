package org.onebusaway.tripplanner.services;

import java.text.DateFormat;
import java.util.Date;

public class StopTimeInstanceProxy {

  private static final DateFormat DAY_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

  private static final DateFormat TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  private long _serviceDate;

  private StopTimeProxy _stopTime;

  public StopTimeInstanceProxy(StopTimeProxy stopTime, Date serviceDate) {
    this(stopTime, serviceDate.getTime());
  }

  public StopTimeInstanceProxy(StopTimeProxy stopTime, long serviceDate) {
    _stopTime = stopTime;
    _serviceDate = serviceDate;
  }

  public StopTimeProxy getStopTime() {
    return _stopTime;
  }

  public String getTripId() {
    return _stopTime.getTripId();
  }

  public int getSequence() {
    return _stopTime.getSequence();
  }

  public StopProxy getStop() {
    return _stopTime.getStop();
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  public long getArrivalTime() {
    return _serviceDate + _stopTime.getArrivalTime() * 1000;
  }

  public long getDepartureTime() {
    return _serviceDate + _stopTime.getDepartureTime() * 1000;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeInstanceProxy))
      return false;
    StopTimeInstanceProxy other = (StopTimeInstanceProxy) obj;
    return _stopTime.equals(other._stopTime) && _serviceDate == other._serviceDate;
  }

  @Override
  public int hashCode() {
    return _stopTime.hashCode() + new Long(_serviceDate).hashCode();
  }

  @Override
  public String toString() {
    return "StopTimeInstanceProxy(trip=" + getTripId() + " service=" + DAY_FORMAT.format(_serviceDate) + " arrival="
        + TIME_FORMAT.format(getArrivalTime()) + " departure=" + TIME_FORMAT.format(getDepartureTime()) + ")";
  }
}
