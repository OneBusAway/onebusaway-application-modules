package org.onebusaway.transit_data_federation.services.predictions;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;

public interface TripTimePredictionDao {

  public void saveTripTimePrediction(TripTimePrediction prediction);

  public void saveTripTimePredictions(List<TripTimePrediction> queue);

  public List<TripTimePrediction> getTripTimePredictionsForTripServiceDateAndTimeRange(
      AgencyAndId tripId, long serviceDate, long fromTime, long toTime);

  public List<TripTimePrediction> getTripTimePredictionsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime);

}
