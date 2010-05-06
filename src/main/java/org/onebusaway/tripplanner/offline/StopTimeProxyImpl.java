package org.onebusaway.tripplanner.offline;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;

import java.io.Serializable;

class StopTimeProxyImpl implements StopTimeProxy, Serializable {

  private static final long serialVersionUID = 1L;

  private final Integer _stopTimeId;
  private final int _arrivalTime;
  private final int _departureTime;
  private final String _tripId;
  private final String _serviceId;
  private final String _routeId;
  private final int _sequence;
  private final StopProxy _stop;

  public StopTimeProxyImpl(StopTime stopTime, StopProxy stop) {
    this(stopTime.getId(), stopTime.getArrivalTime(), stopTime.getDepartureTime(), stopTime.getTrip().getId(),
        stopTime.getTrip().getServiceId(), stopTime.getTrip().getRoute().getId(), stopTime.getStopSequence(), stop);
  }

  public StopTimeProxyImpl(Integer stopTimeId, int arrivalTime, int departureTime, String tripId, String serviceId,
      String routeId, int sequence, StopProxy stop) {
    _stopTimeId = stopTimeId;
    _arrivalTime = arrivalTime;
    _departureTime = departureTime;
    _tripId = tripId;
    _serviceId = serviceId;
    _routeId = routeId;
    _stop = stop;
    _sequence = sequence;
  }

  public Integer getId() {
    return _stopTimeId;
  }

  public int getArrivalTime() {
    return _arrivalTime;
  }

  public int getDepartureTime() {
    return _departureTime;
  }

  public String getRouteId() {
    return _routeId;
  }

  public int getSequence() {
    return _sequence;
  }

  public String getServiceId() {
    return _serviceId;
  }

  public StopProxy getStop() {
    return _stop;
  }

  public String getTripId() {
    return _tripId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeProxyImpl))
      return false;
    StopTimeProxyImpl other = (StopTimeProxyImpl) obj;
    return _stopTimeId.equals(other._stopTimeId);
  }

  @Override
  public int hashCode() {
    return _stopTimeId.hashCode();
  }

  @Override
  public String toString() {
    return "StopTimeProxyImpl(trip=" + _tripId + " serviceId=" + _serviceId + " arrival=" + _arrivalTime
        + " departure=" + _departureTime + ")";
  }

}
