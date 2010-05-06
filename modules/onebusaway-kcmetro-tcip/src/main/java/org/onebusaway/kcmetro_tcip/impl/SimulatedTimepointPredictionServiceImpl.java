package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;
import org.onebusaway.kcmetro_tcip.services.TimepointPredictionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulatedTimepointPredictionServiceImpl implements
    TimepointPredictionService {

  private List<TimepointPredictionListener> _listeners = new ArrayList<TimepointPredictionListener>();

  private List<TimepointPrediction> _timepointPredictions;

  public void setListener(TimepointPredictionListener listener) {
    addListener(listener);
  }

  public void addListener(TimepointPredictionListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(TimepointPredictionListener listener) {
    _listeners.remove(listener);
  }

  public void setTimepointPredictions(
      List<TimepointPrediction> timepointPredictions) {
    _timepointPredictions = timepointPredictions;
  }

  public void startup() {
    for (TimepointPrediction prediction : _timepointPredictions) {
      List<TimepointPrediction> list = Arrays.asList(prediction);
      for (TimepointPredictionListener listener : _listeners)
        listener.handleTimepointPredictions(list);
    }
  }
}
