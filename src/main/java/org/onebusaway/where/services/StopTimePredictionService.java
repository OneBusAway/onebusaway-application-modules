package org.onebusaway.where.services;

import org.onebusaway.where.model.StopTimeInstance;

import java.util.List;

public interface StopTimePredictionService {
  public void getPredictions(List<StopTimeInstance> stopTimes);
}
