package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.StopRealtimeService;
import org.onebusaway.transit_data_federation.services.realtime.TripPosition;
import org.onebusaway.transit_data_federation.services.realtime.TripPositionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

/**
 * Implementation for {@link TripPositionService}. Keeps a recent cache of
 * {@link TripPositionRecord} records for current queries and can access
 * database persisted records for queries in the past.
 * 
 * @author bdferris
 * @see TripPositionService
 */
@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime:name=TripPositionServiceImpl")
public class TripPositionServiceImpl implements TripPositionService,
    StopRealtimeService, VehicleLocationListener {

  /**
   * The radius of the search window we'll use for finding
   * {@link TripPositionRecord} records for a given vehicle id and time
   */
  private static final int TRIP_FOR_VEHICLE_SEARCH_WINDOW = 20 * 60 * 1000;

  private static final long TIME_WINDOW = 30 * 60 * 1000;

  private Cache _tripPositionRecordCollectionCache;

  private TripPositionRecordDao _tripPositionRecordDao;

  private TransitGraphDao _transitGraphDao;

  private CalendarService _calendarService;

  private NarrativeService _narrativeService;

  private ShapePointService _shapePointService;

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _tripPositionRecordCacheWindowSize = 20 * 60;

  private int _predictionCacheMaxOffset = 5 * 60;

  /**
   * If we are interpolating trip position, do we still interpolate when we
   * don't have shape data for a trip?
   */
  private boolean _interpolateWhenNoShapeInfoPresent = true;

  /**
   * Should trip position records be stored to the database?
   */
  private boolean _persistTripPositionRecords = false;

  /**
   * We queue up trip position records so they can be bulk persisted to the
   * database
   */
  private List<TripPositionRecord> _recordPersistenceQueue = new ArrayList<TripPositionRecord>();

  /**
   * Used to schedule periodic flushes to the database of the trip position
   * records queue
   */
  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * Trip position record persistence stats - last record insert duration
   */
  private volatile long _lastInsertDuration = 0;

  /**
   * Trip position record persistence stats - last record insert count
   */
  private volatile long _lastInsertCount = 0;

  /**
   * Records the number of times trip position record cache requests fall
   * through to the database
   */
  private AtomicInteger _tripPositionRecordPersistentStoreAccessCount = new AtomicInteger();

  /**
   * A count of the number of stop times queried for real-time arrival
   * information
   */
  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  /**
   * A count of the number of stop times queried for real-time arrival
   * information that actually had data
   */
  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

  @Autowired
  public void setTripPositionRecordDao(
      TripPositionRecordDao tripPositionRecordDao) {
    _tripPositionRecordDao = tripPositionRecordDao;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  /**
   * The cache we use to temporarily store {@link TripPositionRecordCollection}
   * entries with {@link ServiceDateAndId} (trip+serviced date) keys. The
   * underlying cache is responsible for setting the cache eviction policy.
   * That's a little confusing, since we also have the
   * {@link #setTripPositionRecordCacheWindowSize(int)} which determines how
   * many seconds of memory each record collection will have. However, we won't
   * be checking each collection to see if we haven't had an entry in the last n
   * seconds, so we just leave that to the underlying cache.
   * 
   * 
   * @param cache
   */
  public void setTripPositionRecordCache(Cache cache) {
    _tripPositionRecordCollectionCache = cache;
  }

  /**
   * Controls how far back in time we include records in the
   * {@link TripPositionRecordCollection} for each active trip.
   * 
   * @param windowSize in seconds
   */
  public void setTripPositionRecordCacheWindowSize(int windowSize) {
    _tripPositionRecordCacheWindowSize = windowSize;
  }

  /**
   * Should we interpolate trip position data even when we don't have shape data
   * to work against?
   * 
   * @param interpolateWhenNoShapeInfoPresent
   */
  public void setInterpolateWhenNoShapeInfoPresent(
      boolean interpolateWhenNoShapeInfoPresent) {
    _interpolateWhenNoShapeInfoPresent = interpolateWhenNoShapeInfoPresent;
  }

  /**
   * Should we persist {@link TripPositionRecord} records to an underlying
   * datastore. Useful if you wish to query trip status for historic analysis.
   * 
   * @param persistTripTimePredictions
   */
  public void setPersistTripPositionRecords(boolean persistTripTimePredictions) {
    _persistTripPositionRecords = persistTripTimePredictions;
  }

  /****
   * JMX Attributes
   ****/

  @ManagedAttribute
  public long getLastInsertDuration() {
    return _lastInsertDuration;
  }

  @ManagedAttribute
  public long getLastInsertCount() {
    return _lastInsertCount;
  }

  @ManagedAttribute
  public long getTripPositionRecordPersistentStoreAccessCount() {
    return _tripPositionRecordPersistentStoreAccessCount.get();
  }

  @ManagedAttribute
  public int getStopTimesTotal() {
    return _stopTimesTotal.intValue();
  }

  @ManagedAttribute
  public int getStopTimesWithPredictions() {
    return _stopTimesWithPredictions.intValue();
  }

  /****
   * Setup and Teardown
   ****/

  @PostConstruct
  public void start() {
    if (_persistTripPositionRecords)
      _executor.scheduleAtFixedRate(new PredictionWriter(), 0, 1,
          TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    if (_persistTripPositionRecords)
      _executor.shutdownNow();
  }

  /****
   * {@link VehicleLocationListener} Interface
   ****/

  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    AgencyAndId tripId = record.getTripId();
    long timeOfRecord = record.getCurrentTime();
    int scheduleDeviation = record.getScheduleDeviation();

    TripPositionRecord tripPositionRecord = new TripPositionRecord(tripId,
        record.getServiceDate(), timeOfRecord, scheduleDeviation,
        record.getVehicleId());

    putTripPositionRecord(tripPositionRecord);
  }

  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {

    Map<ServiceDateAndId, List<VehicleLocationRecord>> recordsByTripId = getVehiclePositionRecordsByServiceDateAndTripId(records);

    for (Map.Entry<ServiceDateAndId, List<VehicleLocationRecord>> entry : recordsByTripId.entrySet()) {

      ServiceDateAndId tripId = entry.getKey();
      for (VehicleLocationRecord record : entry.getValue()) {

        long timeOfRecord = record.getCurrentTime();
        int scheduleDeviation = record.getScheduleDeviation();

        TripPositionRecord tripPositionRecord = new TripPositionRecord(
            tripId.getId(), tripId.getServiceDate(), timeOfRecord,
            scheduleDeviation, record.getVehicleId());

        putTripPositionRecord(tripPositionRecord);
      }
    }
  }

  /****
   * {@link StopRealtimeService} Interface
   ****/

  public void applyRealtimeData(List<StopTimeInstanceProxy> stopTimes,
      long targetTime) {

    _stopTimesTotal.addAndGet(stopTimes.size());

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> stisByTripId = getStopTimeInstancesByTripId(stopTimes);

    for (Map.Entry<ServiceDateAndId, List<StopTimeInstanceProxy>> entry : stisByTripId.entrySet()) {
      ServiceDateAndId serviceDateAndId = entry.getKey();
      TripPositionRecordCollection collection = getTripPositionRecordCollectionForTrip(
          serviceDateAndId, targetTime);

      for (StopTimeInstanceProxy sti : entry.getValue()) {

        // Only apply real-time data if there is data to apply
        if (collection.isEmpty())
          continue;

        int scheduleDeviation = collection.getScheduleDeviationForTargetTime(targetTime);
        sti.setPredictedArrivalOffset(scheduleDeviation);
        sti.setPredictedDepartureOffset(scheduleDeviation);
        _stopTimesWithPredictions.incrementAndGet();
      }
    }
  }

  /****
   * {@link TripPositionService} Interface
   ****/

  @Override
  public Map<TripInstanceProxy, TripPosition> getScheduledTripsForBounds(
      CoordinateBounds bounds, long time) {

    CoordinateRectangle r = new CoordinateRectangle(bounds.getMinLat(),
        bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon());
    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(r);

    ServiceIdIntervals intervals = new ServiceIdIntervals();
    for (StopEntry stop : stops) {
      StopTimeIndex index = stop.getStopTimes();
      intervals.addIntervals(index.getServiceIdIntervals());
    }

    long timeFrom = time - TIME_WINDOW;
    long timeTo = time + TIME_WINDOW;

    Map<LocalizedServiceId, List<Date>> serviceIdsAndDates = _calendarService.getServiceDatesWithinRange(
        intervals, new Date(timeFrom), new Date(timeTo));

    Set<TripInstanceProxy> tripInstances = new HashSet<TripInstanceProxy>();

    for (StopEntry stop : stops) {
      StopTimeIndex index = stop.getStopTimes();
      List<StopTimeInstanceProxy> stopTimeInstances = StopTimeSearchOperations.getStopTimeInstancesInRange(
          index, timeFrom, timeTo, StopTimeOp.DEPARTURE, serviceIdsAndDates);
      for (StopTimeInstanceProxy stopTimeInstance : stopTimeInstances) {
        TripEntry trip = stopTimeInstance.getTrip();
        long serviceDate = stopTimeInstance.getServiceDate();
        tripInstances.add(new TripInstanceProxy(trip, serviceDate));
      }
    }

    Map<TripInstanceProxy,TripPosition> tripsAndPositions = new HashMap<TripInstanceProxy, TripPosition>();
    
    for (TripInstanceProxy tripInstance : tripInstances) {

      TripPosition tripPosition = getPositionForTripInstance(
          tripInstance, time);

      if (tripPosition == null)
        continue;

      CoordinatePoint location = tripPosition.getPosition();

      if (location != null
          && bounds.contains(location.getLat(), location.getLon())) {

        tripsAndPositions.put(tripInstance, tripPosition);
      }
    }

    return tripsAndPositions;
  }

  @Override
  public TripPosition getPositionForTripInstance(
      TripInstanceProxy tripInstance, long targetTime) {

    ServiceDateAndId serviceDateAndId = new ServiceDateAndId(
        tripInstance.getServiceDate(), tripInstance.getTrip().getId());

    TripPositionRecordCollection records = getTripPositionRecordCollectionForTrip(
        serviceDateAndId, targetTime);

    return getTripPosition(tripInstance, records, targetTime);
  }

  @Override
  public TripPosition getPositionForVehicleAndTime(AgencyAndId vehicleId,
      long time) {

    long fromTime = time - TRIP_FOR_VEHICLE_SEARCH_WINDOW;
    long toTime = time + TRIP_FOR_VEHICLE_SEARCH_WINDOW;

    List<TripPositionRecord> records = _tripPositionRecordDao.getTripPositionRecordsForVehicleAndTimeRange(
        vehicleId, fromTime, toTime);

    if (records.isEmpty())
      return null;

    Min<TripPositionRecord> closest = new Min<TripPositionRecord>();
    for (TripPositionRecord record : records) {
      closest.add(Math.abs(record.getTime() - time), record);
    }

    TripPositionRecord representative = closest.getMinElement();

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(representative.getTripId());
    TripInstanceProxy tripInstance = new TripInstanceProxy(tripEntry,
        representative.getServiceDate());

    TripPositionRecordCollection collection = TripPositionRecordCollection.createFromRecords(records);

    return getTripPosition(tripInstance, collection, time);
  }

  /****
   * Private Methods
   ****/

  private Map<ServiceDateAndId, List<VehicleLocationRecord>> getVehiclePositionRecordsByServiceDateAndTripId(
      List<VehicleLocationRecord> records) {

    Map<ServiceDateAndId, List<VehicleLocationRecord>> r = new HashMap<ServiceDateAndId, List<VehicleLocationRecord>>();

    for (VehicleLocationRecord record : records) {
      ServiceDateAndId id = new ServiceDateAndId(record.getServiceDate(),
          record.getTripId());
      List<VehicleLocationRecord> recordsForId = r.get(id);
      if (recordsForId == null) {
        recordsForId = new ArrayList<VehicleLocationRecord>();
        r.put(id, recordsForId);
      }
      recordsForId.add(record);
    }

    return r;
  }

  private Map<ServiceDateAndId, List<StopTimeInstanceProxy>> getStopTimeInstancesByTripId(
      List<StopTimeInstanceProxy> stopTimes) {

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> r = new FactoryMap<ServiceDateAndId, List<StopTimeInstanceProxy>>(
        new ArrayList<StopTimeInstanceProxy>());

    for (StopTimeInstanceProxy stopTime : stopTimes) {
      long serviceDate = stopTime.getServiceDate();
      TripEntry trip = stopTime.getTrip();
      AgencyAndId tripId = trip.getId();
      ServiceDateAndId id = new ServiceDateAndId(serviceDate, tripId);
      r.get(id).add(stopTime);
    }
    return r;
  }

  /**
   * We add the {@link TripPositionRecord} to the local cache and persist it to
   * a back-end data-store if necessary
   */
  private void putTripPositionRecord(TripPositionRecord record) {

    ServiceDateAndId serviceDateAndId = new ServiceDateAndId(
        record.getServiceDate(), record.getTripId());

    TripPositionRecordCollection collection = getTripPositionRecordCollectionForTrip(
        serviceDateAndId, record.getTime());

    collection = collection.addRecord(record,
        _tripPositionRecordCacheWindowSize * 1000);

    // Cache the result
    _tripPositionRecordCollectionCache.put(new Element(serviceDateAndId,
        collection));

    if (_persistTripPositionRecords)
      addPredictionToPersistenceQueue(record);
  }

  /**
   * TODO: this method is huge... refactor?
   */
  private TripPosition getTripPosition(TripInstanceProxy tripInstance,
      TripPositionRecordCollection records, long targetTime) {

    TripEntry tripEntry = tripInstance.getTrip();
    long serviceDate = tripInstance.getServiceDate();

    TripPosition position = new TripPosition();

    position.setTripId(tripInstance.getTrip().getId());
    position.setServiceDate(tripInstance.getServiceDate());

    boolean predicted = !(records == null || records.isEmpty());
    position.setPredicted(predicted);

    int scheduleDeviation = predicted
        ? records.getScheduleDeviationForTargetTime(targetTime) : 0;
    position.setScheduleDeviation(scheduleDeviation);

    position.setVehicleId(records.getVehicleId());

    position.setTime(records.getToTime());

    /**
     * Let's interpolate the position of the transit vehicle.
     * 
     */

    /**
     * Effective scheduled time is the point that a transit vehicle is at on its
     * schedule, with schedule deviation taken into account. So if it's 100
     * minutes into the current service date and the bus is running 10 minutes
     * late, it's actually at the 90 minute point in its scheduled operation.
     */
    int effectiveScheduledTime = (int) ((targetTime - serviceDate) / 1000)
        - scheduleDeviation;
    List<StopTimeEntry> stopTimes = tripEntry.getStopTimes();
    StopTimeOp stopTimeOp = StopTimeOp.DEPARTURE;

    int index = StopTimeSearchOperations.searchForStopTime(stopTimes,
        effectiveScheduledTime, stopTimeOp);

    // Did we have a direct hit?
    if (0 <= index && index < stopTimes.size()) {
      StopTimeEntry stopTime = stopTimes.get(index);
      if (stopTime.getArrivalTime() <= effectiveScheduledTime
          && effectiveScheduledTime <= stopTime.getDepartureTime()) {
        StopEntry stop = stopTime.getStop();
        CoordinatePoint location = new CoordinatePoint(stop.getStopLat(),
            stop.getStopLon());
        position.setPosition(location);
        position.setClosestStop(stopTime);
        position.setClosestStopTimeOffset(0);
        return position;
      }
    }

    /**
     * TODO: This might be too stringent. What if a trip is active outside the
     * bounds of its first and last stop time (especially in the case of block
     * trips).
     */
    if (index == 0 || index == stopTimes.size()) {
      // Out of bounds for this trip
      return null;
    }

    StopTimeEntry before = stopTimes.get(index - 1);
    StopTimeEntry after = stopTimes.get(index);

    TripNarrative tripNarrative = _narrativeService.getTripForId(tripEntry.getId());

    AgencyAndId shapeId = tripNarrative.getShapeId();

    int fromTime = before.getDepartureTime();
    int toTime = after.getArrivalTime();

    int fromTimeOffset = fromTime - effectiveScheduledTime;
    int toTimeOffset = toTime - effectiveScheduledTime;

    if (Math.abs(fromTimeOffset) < Math.abs(toTimeOffset)) {
      position.setClosestStop(before);
      position.setClosestStopTimeOffset(fromTimeOffset);
    } else {
      position.setClosestStop(after);
      position.setClosestStopTimeOffset(toTimeOffset);
    }

    double ratio = (effectiveScheduledTime - fromTime)
        / ((double) (toTime - fromTime));

    // Do we have enough information to use shape distance traveled?
    if (shapeId != null && before.getShapeDistTraveled() >= 0
        && after.getShapeDistTraveled() >= 0) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

      if (!shapePoints.isEmpty()) {
        ShapePointIndex shapePointIndexMethod = getShapeDistanceTraveled(
            before, after, ratio);
        CoordinatePoint location = shapePointIndexMethod.getPoint(shapePoints);
        position.setPosition(location);
        return position;
      }
    }

    if (!_interpolateWhenNoShapeInfoPresent)
      return null;

    StopEntry beforeStop = before.getStop();
    StopEntry afterStop = after.getStop();
    double latFrom = beforeStop.getStopLat();
    double lonFrom = beforeStop.getStopLon();
    double latTo = afterStop.getStopLat();
    double lonTo = afterStop.getStopLon();
    double lat = (latTo - latFrom) * ratio + latFrom;
    double lon = (lonTo - lonFrom) * ratio + lonFrom;

    CoordinatePoint location = new CoordinatePoint(lat, lon);
    position.setPosition(location);
    return position;
  }

  private ShapePointIndex getShapeDistanceTraveled(
      StopTimeEntry beforeStopTime, StopTimeEntry afterStopTime,
      double ratio) {

    double fromDistance = beforeStopTime.getShapeDistTraveled();
    double toDistance = afterStopTime.getShapeDistTraveled();

    double distance = ratio * (toDistance - fromDistance) + fromDistance;
    return new DistanceTraveledShapePointIndex(distance);
  }

  private TripPositionRecordCollection getTripPositionRecordCollectionForTrip(
      ServiceDateAndId serviceDateAndId, long targetTime) {

    Element element = _tripPositionRecordCollectionCache.get(serviceDateAndId);

    // Did we have an entry in the cache already?
    if (element != null) {
      TripPositionRecordCollection entry = (TripPositionRecordCollection) element.getValue();
      long offset = _predictionCacheMaxOffset * 1000;
      if (entry.getFromTime() - offset <= targetTime
          && targetTime <= entry.getToTime() + offset)
        return entry;
    }

    long offset = _tripPositionRecordCacheWindowSize * 1000 / 2;
    long fromTime = targetTime - offset;
    long toTime = targetTime + offset;
    SortedMap<Long, Integer> scheduleDeviations = new TreeMap<Long, Integer>();

    // We only consult persisted cache entries if the requested target time is
    // not within our current cache window
    if (targetTime + offset < System.currentTimeMillis()
        && _persistTripPositionRecords) {

      _tripPositionRecordPersistentStoreAccessCount.incrementAndGet();

      List<TripPositionRecord> predictions = _tripPositionRecordDao.getTripPositionRecordsForTripServiceDateAndTimeRange(
          serviceDateAndId.getId(), serviceDateAndId.getServiceDate(),
          fromTime, toTime);

      if (!predictions.isEmpty())
        return TripPositionRecordCollection.createFromRecords(predictions);
    }

    return new TripPositionRecordCollection(fromTime, toTime,
        scheduleDeviations);
  }

  private void addPredictionToPersistenceQueue(TripPositionRecord prediction) {
    synchronized (_recordPersistenceQueue) {
      _recordPersistenceQueue.add(prediction);
    }
  }

  private List<TripPositionRecord> getPredictionPersistenceQueue() {
    synchronized (_recordPersistenceQueue) {
      List<TripPositionRecord> queue = new ArrayList<TripPositionRecord>(
          _recordPersistenceQueue);
      _recordPersistenceQueue.clear();
      return queue;
    }
  }

  private class PredictionWriter implements Runnable {

    @Override
    public void run() {

      List<TripPositionRecord> queue = getPredictionPersistenceQueue();

      if (queue.isEmpty())
        return;

      long t1 = System.currentTimeMillis();
      _tripPositionRecordDao.saveTripPositionRecords(queue);
      long t2 = System.currentTimeMillis();
      _lastInsertDuration = t2 - t1;
      _lastInsertCount = queue.size();
    }
  }
}
