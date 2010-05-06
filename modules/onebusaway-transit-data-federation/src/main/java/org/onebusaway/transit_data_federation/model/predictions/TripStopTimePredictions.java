/**
 * 
 */
package org.onebusaway.transit_data_federation.model.predictions;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.onebusaway.utility.InterpolationStrategy;
import org.onebusaway.utility.InterpolationLibrary.EOutOfRangeStrategy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class TripStopTimePredictions implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final StopTimePredictionInterpolationStrategy _interpolationStrategy = new StopTimePredictionInterpolationStrategy();

  private SortedMap<Integer, StopTimePrediction> _predictionsByScheduledTime = new TreeMap<Integer, StopTimePrediction>();

  private long _prunePredictionsAfterThreshold;

  public TripStopTimePredictions(long prunePredictionsAfterThreshold) {
    _prunePredictionsAfterThreshold = prunePredictionsAfterThreshold;
  }

  public void addPredictions(List<StopTimePrediction> predictions) {
    for (StopTimePrediction prediction : predictions) {
      /**
       * Why do we add the prediction to the sorted map twice? We add it for
       * both the scheduled arrival AND departure times for cases when they are
       * different (when they are the same, the prediction is effectively added
       * just once). When we go back and do prediction interpolation, we can
       * detect when we are attempting to interpolate between the scheduled
       * arrival and departure time for a single StopTimepPrediction member and
       * just return that member directly.
       */
      _predictionsByScheduledTime.put(prediction.getScheduledArrivalTime(),
          prediction);
      _predictionsByScheduledTime.put(prediction.getScheduledDepartureTime(),
          prediction);
    }
  }

  public void applyPredictions(StopTimeInstanceProxy sti) {

    pruneStalePredictions();

    if (_predictionsByScheduledTime.isEmpty())
      return;

    StopTimeEntry stopTime = sti.getStopTime();
    int at = stopTime.getArrivalTime();
    int dt = stopTime.getDepartureTime();

    StopTimePrediction arrivalPrediction = getStopTimePredictionForScheduledTime(at);
    StopTimePrediction departurePrediction = getStopTimePredictionForScheduledTime(dt);

    sti.setPredictedArrivalOffset(arrivalPrediction.getPredictedArrivalOffset());
    sti.setPredictedDepartureOffset(departurePrediction.getPredictedDepartureOffset());
  }

  public int getPredictedOffsetForScheduledTime(int time) {
    StopTimePrediction prediction = getStopTimePredictionForScheduledTime(time);
    return prediction.getPredictedArrivalOffset();
  }

  private StopTimePrediction getStopTimePredictionForScheduledTime(int time) {
    
    int firstKey = _predictionsByScheduledTime.firstKey();
    int lastKey = _predictionsByScheduledTime.lastKey();

    if (time < firstKey) {
      StopTimePrediction firstPrediction = _predictionsByScheduledTime.get(firstKey);
      StopTimePrediction toReturn = new StopTimePrediction();
      toReturn.setPredictedArrivalOffset(firstPrediction.getPredictedArrivalOffset());
      toReturn.setPredictedDepartureOffset(firstPrediction.getPredictedArrivalOffset());
      return toReturn;
    } else if (lastKey <= time) {
      StopTimePrediction lastPrediction = _predictionsByScheduledTime.get(lastKey);
      StopTimePrediction toReturn = new StopTimePrediction();
      toReturn.setPredictedArrivalOffset(lastPrediction.getPredictedDepartureOffset());
      toReturn.setPredictedDepartureOffset(lastPrediction.getPredictedDepartureOffset());
      return toReturn;
    }

    return InterpolationLibrary.interpolate(_interpolationStrategy,
        EOutOfRangeStrategy.EXCEPTION, _predictionsByScheduledTime, time);
  }

  private void pruneStalePredictions() {
    long now = System.currentTimeMillis();

    for (Iterator<StopTimePrediction> it = _predictionsByScheduledTime.values().iterator(); it.hasNext();) {
      StopTimePrediction prediction = it.next();
      if (now - prediction.getPredictionTime() > _prunePredictionsAfterThreshold)
        it.remove();
    }
  }

  private static class StopTimePredictionInterpolationStrategy implements
      InterpolationStrategy<Integer, StopTimePrediction> {

    @Override
    public StopTimePrediction interpolate(Integer prevKey,
        StopTimePrediction prevValue, Integer nextKey,
        StopTimePrediction nextValue, double ratio) {

      if (prevValue == nextValue)
        return prevValue;

      int predictedOffset = (int) InterpolationLibrary.interpolatePair(
          prevValue.getPredictedDepartureOffset(),
          nextValue.getPredictedArrivalOffset(), ratio);

      StopTimePrediction prediction = new StopTimePrediction();
      prediction.setPredictedArrivalOffset(predictedOffset);
      prediction.setPredictedDepartureOffset(predictedOffset);

      return prediction;
    }

  }
}