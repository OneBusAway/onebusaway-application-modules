package org.onebusaway.transit_data_federation.services;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

public interface TripPositionService {

  public CoordinatePoint getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime);

}
