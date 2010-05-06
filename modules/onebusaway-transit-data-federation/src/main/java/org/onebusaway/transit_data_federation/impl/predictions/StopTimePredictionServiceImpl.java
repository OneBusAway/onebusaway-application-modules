package org.onebusaway.transit_data_federation.impl.predictions;

import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;
import org.onebusaway.transit_data_federation.model.predictions.TripStopTimePredictions;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionService;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionSourceService;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionSourceServiceListener;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import edu.washington.cs.rse.collections.FactoryMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link StopTimePrediction} aggregator that listens for predictions from
 * multiple sources, integrating the predictions into a common model so that
 * predictions can be applied to {@link StopTimeInstanceProxy} objects.
 * 
 * @author bdferris
 * 
 */
@Component
class StopTimePredictionServiceImpl implements StopTimePredictionService,
    StopTimePredictionSourceServiceListener {

  static final long PRUNE_STOP_TIME_PREDICTIONS_THRESHOLD = 5 * 60 * 1000;

  private Cache _tripStopTimePredictionsCache;

  private TripTimePredictionService _tripTimePredictionService;

  public void setTripStopTimePredictionsCache(Cache cache) {
    _tripStopTimePredictionsCache = cache;
  }

  public void setStopTimePredictionSources(
      List<StopTimePredictionSourceService> stopTimePredictionSources) {
    for (StopTimePredictionSourceService source : stopTimePredictionSources)
      source.addListener(this);
  }

  public void setTripTimePredictionService(
      TripTimePredictionService tripTimePredictionService) {
    _tripTimePredictionService = tripTimePredictionService;
  }

  /****
   * {@link StopTimePredictionService} Interface
   ****/

  public void applyPredictions(List<StopTimeInstanceProxy> stopTimes) {

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> stisByTripId = getStopTimeInstancesByTripIdInstance(stopTimes);

    for (Map.Entry<ServiceDateAndId, List<StopTimeInstanceProxy>> entry : stisByTripId.entrySet()) {
      ServiceDateAndId id = entry.getKey();
      TripStopTimePredictions predictions = getPredictionsForTripId(id);
      for (StopTimeInstanceProxy sti : entry.getValue())
        predictions.applyPredictions(sti);
    }
  }

  /****
   * {@link StopTimePredictionSourceServiceListener} Interface
   ****/

  public void handleStopTimePredictions(List<StopTimePrediction> predictions) {

    Map<ServiceDateAndId, List<StopTimePrediction>> predictionsByTripId = getStopTimePredictionsByTripIdInstance(predictions);

    for (Map.Entry<ServiceDateAndId, List<StopTimePrediction>> entry : predictionsByTripId.entrySet()) {

      ServiceDateAndId id = entry.getKey();
      List<StopTimePrediction> newPredictions = entry.getValue();

      TripStopTimePredictions tripPredictions = getPredictionsForTripId(id);
      tripPredictions.addPredictions(newPredictions);
      setPredictionsForTripId(id, tripPredictions);

      recordTripTimePrediction(id, newPredictions, tripPredictions);
    }
  }

  /****
   * Private Methods
   ****/

  private Map<ServiceDateAndId, List<StopTimeInstanceProxy>> getStopTimeInstancesByTripIdInstance(
      List<StopTimeInstanceProxy> stopTimes) {
    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> r = new FactoryMap<ServiceDateAndId, List<StopTimeInstanceProxy>>(
        new ArrayList<StopTimeInstanceProxy>());
    for (StopTimeInstanceProxy stopTime : stopTimes) {
      ServiceDateAndId id = new ServiceDateAndId(stopTime.getServiceDate(),
          stopTime.getTrip().getId());
      r.get(id).add(stopTime);
    }
    return r;
  }

  private Map<ServiceDateAndId, List<StopTimePrediction>> getStopTimePredictionsByTripIdInstance(
      List<StopTimePrediction> predictions) {
    Map<ServiceDateAndId, List<StopTimePrediction>> r = new FactoryMap<ServiceDateAndId, List<StopTimePrediction>>(
        new ArrayList<StopTimePrediction>());
    for (StopTimePrediction prediction : predictions) {
      ServiceDateAndId id = new ServiceDateAndId(prediction.getServiceDate(),
          prediction.getTripId());
      r.get(id).add(prediction);
    }
    return r;
  }

  private void recordTripTimePrediction(ServiceDateAndId id,
      List<StopTimePrediction> newPredictions,
      TripStopTimePredictions tripPredictions) {

    if (_tripTimePredictionService == null)
      return;

    long maxPredictionTime = Long.MIN_VALUE;

    for (StopTimePrediction prediction : newPredictions)
      maxPredictionTime = Math.max(maxPredictionTime,
          prediction.getPredictionTime());

    long serviceDate = id.getServiceDate();
    int scheduledTime = (int) ((maxPredictionTime - serviceDate) / 1000);

    int offset = tripPredictions.getPredictedOffsetForScheduledTime(scheduledTime);

    _tripTimePredictionService.putScheduleDeviationPrediction(id.getId(),
        serviceDate, maxPredictionTime, offset);
  }

  private TripStopTimePredictions getPredictionsForTripId(ServiceDateAndId id) {
    Element element = _tripStopTimePredictionsCache.get(id);
    if (element == null)
      return new TripStopTimePredictions(PRUNE_STOP_TIME_PREDICTIONS_THRESHOLD);
    return (TripStopTimePredictions) element.getValue();
  }

  private void setPredictionsForTripId(ServiceDateAndId id,
      TripStopTimePredictions tripPredictions) {
    Element element = new Element(id, tripPredictions);
    _tripStopTimePredictionsCache.put(element);
  }
}
