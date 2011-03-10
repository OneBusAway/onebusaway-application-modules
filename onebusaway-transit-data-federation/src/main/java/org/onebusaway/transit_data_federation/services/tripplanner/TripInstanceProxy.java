package org.onebusaway.transit_data_federation.services.tripplanner;

/**
 * A trip instance is the combination of a {@link TripEntry} and a service date
 * for which that trip is active. The "service date" is the "midnight time" from
 * which the {@link StopTimeEntry} entries are relative.
 * 
 * @author bdferris
 * @see TripEntry
 */
public class TripInstanceProxy {

  private final TripEntry _trip;

  private final long _serviceDate;

  public TripInstanceProxy(TripEntry trip, long serviceDate) {
    if (trip == null)
      throw new IllegalArgumentException();
    _trip = trip;
    _serviceDate = serviceDate;
  }

  public TripEntry getTrip() {
    return _trip;
  }

  /**
   * The service date that the trip instance is operating. This is the
   * "midnight" time relative to the stop times for the trip.
   * 
   * @return the service date on which the trip is operating (Unix-time)
   */
  public long getServiceDate() {
    return _serviceDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_serviceDate ^ (_serviceDate >>> 32));
    result = prime * result + ((_trip == null) ? 0 : _trip.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TripInstanceProxy))
      return false;
    TripInstanceProxy other = (TripInstanceProxy) obj;
    if (_serviceDate != other._serviceDate)
      return false;
    if (!_trip.equals(other._trip))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _trip.toString() + " " + _serviceDate;
  }

}
