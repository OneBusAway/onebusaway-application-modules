package org.onebusaway.transit_data_federation.services.predictions;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import java.util.List;

public interface StopTimePredictionService {
  public void applyPredictions(List<StopTimeInstanceProxy> stopTimes);
}
