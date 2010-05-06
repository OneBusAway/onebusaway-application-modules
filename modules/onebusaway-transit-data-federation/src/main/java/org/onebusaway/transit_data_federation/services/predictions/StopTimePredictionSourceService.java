package org.onebusaway.transit_data_federation.services.predictions;

public interface StopTimePredictionSourceService {
  public void addListener(StopTimePredictionSourceServiceListener listener);
}
