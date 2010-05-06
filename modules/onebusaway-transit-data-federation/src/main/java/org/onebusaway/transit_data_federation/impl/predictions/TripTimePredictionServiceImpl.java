package org.onebusaway.transit_data_federation.impl.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class TripTimePredictionServiceImpl implements TripTimePredictionService {

  private Cache _tripTimePredictionCache;

  private TransitDataFederationMutableDao _dao;

  private int _predictionCacheWindowSize = 30 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

  private boolean _persistTripTimePredictions = false;

  @Autowired
  public void setTransitDataFederationMutableDao(
      TransitDataFederationMutableDao dao) {
    _dao = dao;
  }

  public void setTripTimePredictionsCache(Cache cache) {
    _tripTimePredictionCache = cache;
  }

  public void setPredictionCacheWindowSize(int windowSize) {
    _predictionCacheWindowSize = windowSize;
  }

  public void setPersistTripTimePredictions(boolean persistTripTimePredictions) {
    _persistTripTimePredictions = persistTripTimePredictions;
  }

  @Override
  public int getScheduledDeviationPrediction(AgencyAndId tripId,
      long serviceDate, long targetTime) {

    TripTimePredictionsEntry cacheEntry = getTripTimePredictionCacheEntryForTrip(
        tripId, serviceDate, targetTime);

    if (cacheEntry == null)
      return 0;

    return cacheEntry.getScheduleDeviationForTargetTime(targetTime);
  }

  @Override
  public void putScheduleDeviationPrediction(AgencyAndId tripId,
      long serviceDate, long time, int scheduleDeviation) {

    TripTimePrediction prediction = new TripTimePrediction(tripId, serviceDate,
        time, scheduleDeviation);

    if (_persistTripTimePredictions)
      _dao.save(prediction);

    TripTimePredictionsEntry entry = getTripTimePredictionCacheEntryForTrip(
        tripId, serviceDate, time);

    entry = entry.addPrediction(time, scheduleDeviation,
        _predictionCacheWindowSize * 1000);

    // Cache the result
    ServiceDateAndId id = new ServiceDateAndId(serviceDate, tripId);
    _tripTimePredictionCache.put(new Element(id, entry));
  }

  private TripTimePredictionsEntry getTripTimePredictionCacheEntryForTrip(
      AgencyAndId tripId, long serviceDate, long targetTime) {

    ServiceDateAndId fullId = new ServiceDateAndId(serviceDate, tripId);
    Element element = _tripTimePredictionCache.get(fullId);

    if (element != null) {
      TripTimePredictionsEntry entry = (TripTimePredictionsEntry) element.getValue();
      long offset = _predictionCacheMaxOffset * 1000;
      if (entry.getFromTime() - offset <= targetTime
          && targetTime <= entry.getToTime() + offset)
        return entry;
    }

    long offset = _predictionCacheWindowSize * 1000 / 2;
    long fromTime = targetTime - offset;
    long toTime = targetTime + offset;
    SortedMap<Long, Integer> scheduleDeviations = new TreeMap<Long, Integer>();

    if (_persistTripTimePredictions) {
      List<TripTimePrediction> predictions = _dao.getTripTimePredictionsForTripServiceDateAndTimeRange(
          tripId, serviceDate, fromTime, toTime);
      for (TripTimePrediction prediction : predictions)
        scheduleDeviations.put(prediction.getTime(),
            prediction.getScheduleDeviation());
    }

    return new TripTimePredictionsEntry(fromTime, toTime, scheduleDeviations);
  }
}
