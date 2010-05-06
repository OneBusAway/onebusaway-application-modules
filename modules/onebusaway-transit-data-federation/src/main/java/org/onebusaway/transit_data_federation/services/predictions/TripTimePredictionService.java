package org.onebusaway.transit_data_federation.services.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;

public interface TripTimePredictionService {
  
  public int getScheduledDeviationPrediction(AgencyAndId tripId, long serviceDate,
      long targetTime);
  
  public void putScheduleDeviationPrediction(AgencyAndId tripId, long serviceDate, long time,
      int scheduleDeviation);
}
