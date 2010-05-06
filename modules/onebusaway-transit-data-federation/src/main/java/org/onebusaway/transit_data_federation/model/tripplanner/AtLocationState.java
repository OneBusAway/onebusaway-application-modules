package org.onebusaway.transit_data_federation.model.tripplanner;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class AtLocationState extends TripState {

  private final CoordinatePoint _location;

  public AtLocationState(long currentTime, CoordinatePoint location) {
    super(currentTime);
    _location = location;
  }

  public CoordinatePoint getLocation() {
    return _location;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_location == null) ? 0 : _location.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AtLocationState other = (AtLocationState) obj;
    return _location.equals(other._location);
  }
}
