package org.onebusaway.transit_data_federation.services.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.predictions.TripStopTimePredictions;

public interface TripStopTimeCacheService {
  
  public TripStopTimePredictions getTripStopTimePredictionsForTripId(
      AgencyAndId tripId);

  public void setTripStopTimePredictionsForTripId(AgencyAndId tripId,
      TripStopTimePredictions tripPredictions);
}
