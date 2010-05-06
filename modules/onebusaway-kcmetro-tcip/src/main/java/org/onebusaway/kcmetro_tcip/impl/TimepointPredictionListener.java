package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;

import java.util.List;

public interface TimepointPredictionListener {
  public void handleTimepointPredictions(List<TimepointPrediction> predictions);
}
