package org.onebusaway.kcmetro_tcip.services;

import org.onebusaway.kcmetro_tcip.impl.TimepointPredictionListener;

public interface TimepointPredictionService {
  public void setListener(TimepointPredictionListener listener);
}
