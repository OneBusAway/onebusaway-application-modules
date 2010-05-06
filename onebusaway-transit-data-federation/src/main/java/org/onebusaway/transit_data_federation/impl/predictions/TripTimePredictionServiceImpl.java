package org.onebusaway.transit_data_federation.impl.predictions;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionDao;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.collections.stats.Min;

@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.predictions:name=TripTimePredictionServiceImpl")
public class TripTimePredictionServiceImpl implements TripTimePredictionService {

  private static final int TIME_WINDOW = 20 * 60 * 1000;

  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  private Cache _tripTimePredictionCache;

  private TripTimePredictionDao _dao;

  private TransitGraphDao _graph;

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _predictionCacheWindowSize = 20 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

  private boolean _persistTripTimePredictions = false;

  private List<TripTimePrediction> _predictionPersistenceQueue = new ArrayList<TripTimePrediction>();

  private volatile long _lastInsertTime = 0;

  private volatile long _lastInsertCount = 0;

  private AtomicInteger _tripTimePredictionCacheEntryForTripAccessCount = new AtomicInteger();

  @Autowired
  public void setTripTimePredictionDao(TripTimePredictionDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
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

  @ManagedAttribute
  public long getLastInsertTime() {
    return _lastInsertTime;
  }

  @ManagedAttribute
  public long getLastInsertCount() {
    return _lastInsertCount;
  }

  @ManagedAttribute
  public long getTripTimePredictionCacheEntryForTripAccessCount() {
    return _tripTimePredictionCacheEntryForTripAccessCount.get();
  }

  @PostConstruct
  public void start() {
    if (_persistTripTimePredictions)
      _executor.scheduleAtFixedRate(new PredictionWriter(), 0, 1,
          TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    if (_persistTripTimePredictions)
      _executor.shutdownNow();
  }

  @Override
  public ScheduleDeviation getScheduledDeviationPrediction(AgencyAndId tripId,
      long serviceDate, long targetTime) {

    TripTimePredictionsEntry cacheEntry = getTripTimePredictionCacheEntryForTrip(
        tripId, serviceDate, targetTime, null);

    if (cacheEntry == null || cacheEntry.isEmpty()) {
      ScheduleDeviation sd = new ScheduleDeviation();
      sd.setPredicted(false);
      sd.setScheduleDeviation(0);
      return sd;
    }

    int deviation = cacheEntry.getScheduleDeviationForTargetTime(targetTime);

    ScheduleDeviation sd = new ScheduleDeviation();
    sd.setPredicted(true);
    sd.setScheduleDeviation(deviation);
    return sd;
  }

  @Override
  public void putScheduleDeviationPrediction(TripTimePrediction prediction) {

    if (_persistTripTimePredictions)
      addPredictionToPersistenceQueue(prediction);

    AgencyAndId tripId = prediction.getTripId();
    long serviceDate = prediction.getServiceDate();
    long time = prediction.getTime();
    int scheduleDeviation = prediction.getScheduleDeviation();

    TripTimePredictionsEntry entry = getTripTimePredictionCacheEntryForTrip(
        tripId, serviceDate, time, prediction.getVehicleId());

    entry = entry.addPrediction(time, scheduleDeviation,
        _predictionCacheWindowSize * 1000);

    // Cache the result
    ServiceDateAndId id = new ServiceDateAndId(serviceDate, tripId);
    _tripTimePredictionCache.put(new Element(id, entry));
  }

  @Override
  public StopTimeEntry getClosestStopForVehicleAndTime(AgencyAndId vehicleId,
      long time) {

    long fromTime = time - TIME_WINDOW;
    long toTime = time + TIME_WINDOW;

    List<TripTimePrediction> predictions = _dao.getTripTimePredictionsForVehicleAndTimeRange(
        vehicleId, fromTime, toTime);
    Min<TripTimePrediction> min = new Min<TripTimePrediction>();

    for (TripTimePrediction prediction : predictions) {
      long offset = Math.abs(prediction.getTime() - time);
      min.add(offset, prediction);
    }

    if (min.isEmpty()) {
      return null;
    }

    TripTimePrediction prediction = min.getMinElement();
    long serviceDate = prediction.getServiceDate();

    Min<StopTimeEntry> closestStopTimes = new Min<StopTimeEntry>();

    long targetTime = time - prediction.getScheduleDeviation() * 1000;

    TripEntry tripEntry = _graph.getTripEntryForId(prediction.getTripId());
    for (StopTimeEntry entry : tripEntry.getStopTimes()) {
      long arrivalTime = serviceDate + entry.getArrivalTime() * 1000;
      long departureTime = serviceDate + entry.getDepartureTime() * 1000;
      if (arrivalTime <= targetTime && targetTime <= departureTime) {
        closestStopTimes.add(0, entry);
      } else {
        closestStopTimes.add(Math.abs(targetTime - arrivalTime), entry);
        closestStopTimes.add(Math.abs(targetTime - departureTime), entry);
      }
    }

    return closestStopTimes.getMinElement();
  }

  /****
   * Private Methods
   ****/

  private TripTimePredictionsEntry getTripTimePredictionCacheEntryForTrip(
      AgencyAndId tripId, long serviceDate, long targetTime, AgencyAndId vehicleId) {

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

    // We only consult persisted cache entries if the requested target time is
    // not within our current cache window
    if (targetTime + offset < System.currentTimeMillis()
        && _persistTripTimePredictions) {
      List<TripTimePrediction> predictions = _dao.getTripTimePredictionsForTripServiceDateAndTimeRange(
          tripId, serviceDate, fromTime, toTime);
      for (TripTimePrediction prediction : predictions)
        scheduleDeviations.put(prediction.getTime(),
            prediction.getScheduleDeviation());
      _tripTimePredictionCacheEntryForTripAccessCount.incrementAndGet();
    }

    return new TripTimePredictionsEntry(fromTime, toTime, scheduleDeviations, vehicleId);
  }

  private void addPredictionToPersistenceQueue(TripTimePrediction prediction) {
    synchronized (_predictionPersistenceQueue) {
      _predictionPersistenceQueue.add(prediction);
    }
  }

  private List<TripTimePrediction> getPredictionPersistenceQueue() {
    synchronized (_predictionPersistenceQueue) {
      List<TripTimePrediction> queue = new ArrayList<TripTimePrediction>(
          _predictionPersistenceQueue);
      _predictionPersistenceQueue.clear();
      return queue;
    }
  }

  private class PredictionWriter implements Runnable {

    @Override
    public void run() {
      
      List<TripTimePrediction> queue = getPredictionPersistenceQueue();
      
      if( queue.isEmpty() )
        return;
      
      long t1 = System.currentTimeMillis();
      _dao.saveTripTimePredictions(queue);
      long t2 = System.currentTimeMillis();
      _lastInsertTime = t2 - t1;
      _lastInsertCount = queue.size();
    }
  }
}
