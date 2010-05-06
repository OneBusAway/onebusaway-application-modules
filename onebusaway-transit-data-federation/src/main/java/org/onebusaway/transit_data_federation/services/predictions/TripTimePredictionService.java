package org.onebusaway.transit_data_federation.services.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public interface TripTimePredictionService {

  public ScheduleDeviation getScheduledDeviationPrediction(AgencyAndId tripId,
      long serviceDate, long targetTime);

  /**
   * 
   * @param tripId the trip id
   * @param serviceDate the service date
   * @param time - the time at which the prediction was made
   * @param scheduleDeviation - the schedule deviation in seconds
   */
  public void putScheduleDeviationPrediction(TripTimePrediction prediction);
  
  public StopTimeEntry getClosestStopForVehicleAndTime(AgencyAndId vehicleId, long time);
}
