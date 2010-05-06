package org.onebusaway.transit_data_federation.services.predictions;

import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;

import java.util.List;

public interface StopTimePredictionListener {
  public void handleStopTimePredictions(List<StopTimePrediction> predictions);
}
