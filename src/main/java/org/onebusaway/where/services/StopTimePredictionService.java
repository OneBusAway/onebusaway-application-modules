package org.onebusaway.where.services;

import org.onebusaway.where.model.StopTimeInstance;
import org.onebusaway.where.model.TimepointPredictionSummary;

import java.util.List;

public interface StopTimePredictionService {
  public void getPredictions(List<StopTimeInstance> stopTimes);
  public TimepointPredictionSummary getPredictionSummary(String tripId);
}
