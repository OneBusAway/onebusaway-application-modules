package org.onebusaway.transit_data_federation.services;

import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

public interface TripPositionService {

  public TripPosition getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime);
}
