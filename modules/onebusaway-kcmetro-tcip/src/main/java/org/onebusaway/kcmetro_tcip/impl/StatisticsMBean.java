package org.onebusaway.kcmetro_tcip.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.kcmetro_tcip:name=statistics")
public class StatisticsMBean {

  private TimepointPredictionServiceImpl _timepointPredictionService;

  private TimepointPredictionToPiSchedAdherenceCountdownServiceImpl _timepointPredictionToPiSchedAdherenceCountdownService;

  @Autowired
  public void setTimepointPredictionService(
      TimepointPredictionServiceImpl timepointPredictionService) {
    _timepointPredictionService = timepointPredictionService;
  }

  @Autowired
  public void setTimepointPredictionToPiSchedAdherenceCountdownServiceImpl(
      TimepointPredictionToPiSchedAdherenceCountdownServiceImpl service) {
    _timepointPredictionToPiSchedAdherenceCountdownService = service;
  }

  @ManagedAttribute()
  public int getRawPredictionCount() {
    return _timepointPredictionService.getPredictionCount();
  }

  @ManagedAttribute()
  public int getRawMappedTripIdCount() {
    return _timepointPredictionService.getMappedTripIdCount();
  }

  @ManagedAttribute
  public int getPredictionsCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionsCount();
  }

  @ManagedAttribute
  public int getPredictionsMappedToStopCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionsMappedToStopCount();
  }

  @ManagedAttribute
  public int getPredictionWithNoTimepointToStopMappingCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionWithNoTimepointToStopMappingCount();
  }

  @ManagedAttribute
  public int getPredictionsWithoutTrackingDataCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionsWithoutTrackingDataCount();
  }

  @ManagedAttribute
  public int getPredictionsWithNegativeTripIdCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionsWithNegativeTripIdCount();
  }

  @ManagedAttribute
  public int getPredictionsWithUnknownTimepointCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getPredictionsWithUnknownTimepointCount();
  }

  @ManagedAttribute
  public int getUnknownTripCount() {
    return _timepointPredictionToPiSchedAdherenceCountdownService.getUnknownTripCount();
  }

}
