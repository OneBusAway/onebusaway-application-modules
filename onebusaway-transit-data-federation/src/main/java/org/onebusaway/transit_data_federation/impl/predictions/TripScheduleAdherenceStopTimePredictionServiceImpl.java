package org.onebusaway.transit_data_federation.impl.predictions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleAdherenceEntries;
import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionService;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleAdherenceListener;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleAdherenceRecord;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import edu.washington.cs.rse.collections.FactoryMap;

/**
 * A {@link StopTimePrediction} aggregator that listens for predictions from
 * multiple sources, integrating the predictions into a common model so that
 * predictions can be applied to {@link StopTimeInstanceProxy} objects.
 * 
 * @author bdferris
 * 
 */
@ManagedResource("org.onebusaway.transit_data_federation.impl.predictions:name=TripScheduleAdherenceStopTimePredictionServiceImpl")
public class TripScheduleAdherenceStopTimePredictionServiceImpl implements
    StopTimePredictionService, ScheduleAdherenceListener {

  static final long PRUNE_STOP_TIME_PREDICTIONS_THRESHOLD = 5 * 60 * 1000;

  private Cache _tripScheduleAdherenceCache;

  private TripTimePredictionService _tripTimePredictionService;

  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

  public void setTripStopTimePredictionsCache(Cache cache) {
    _tripScheduleAdherenceCache = cache;
  }

  public void setTripTimePredictionService(
      TripTimePredictionService tripTimePredictionService) {
    _tripTimePredictionService = tripTimePredictionService;
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
   * {@link StopTimePredictionService} Interface
   ****/

  public void applyPredictions(List<StopTimeInstanceProxy> stopTimes) {

    _stopTimesTotal.addAndGet(stopTimes.size());

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> stisByTripId = getStopTimeInstancesByTripId(stopTimes);

    for (Map.Entry<ServiceDateAndId, List<StopTimeInstanceProxy>> entry : stisByTripId.entrySet()) {
      ServiceDateAndId tripId = entry.getKey();
      ScheduleAdherenceEntries entries = getPredictionsForTripId(tripId);
      for (StopTimeInstanceProxy sti : entry.getValue()) {
        if (entries.applyPredictions(sti))
          _stopTimesWithPredictions.incrementAndGet();
      }
    }
  }

  /****
   * {@link ScheduleAdherenceListener} Interface
   ****/

  public void handleScheduleAdherenceRecords(
      List<ScheduleAdherenceRecord> records) {

    Map<ServiceDateAndId, ScheduleAdherenceRecord> recordsByTripId = getScheduleAdherenceRecordsByTripIdInstance(records);

    for (Map.Entry<ServiceDateAndId, ScheduleAdherenceRecord> entry : recordsByTripId.entrySet()) {

      ServiceDateAndId tripId = entry.getKey();
      ScheduleAdherenceRecord record = entry.getValue();

      ScheduleAdherenceEntries entries = getPredictionsForTripId(tripId);
      entries.addPredictions(record);
      setPredictionsForTripId(tripId, entries);
      recordTripTimePrediction(tripId, record);
    }
  }

  /****
   * Private Methods
   ****/

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

  private Map<ServiceDateAndId, ScheduleAdherenceRecord> getScheduleAdherenceRecordsByTripIdInstance(
      List<ScheduleAdherenceRecord> records) {

    Map<ServiceDateAndId, ScheduleAdherenceRecord> r = new HashMap<ServiceDateAndId, ScheduleAdherenceRecord>();

    for (ScheduleAdherenceRecord record : records) {
      ServiceDateAndId id = new ServiceDateAndId(record.getServiceDate(),
          record.getTripId());
      r.put(id, record);
    }

    return r;
  }

  private void recordTripTimePrediction(ServiceDateAndId tripId,
      ScheduleAdherenceRecord record) {

    if (_tripTimePredictionService == null)
      return;

    long timeOfPrediction = record.getCurrentTime();
    int offset = record.getScheduleDeviation();

    TripTimePrediction tripTimePrediction = new TripTimePrediction(tripId.getId(),
        tripId.getServiceDate(), timeOfPrediction, offset, record.getVehicleId());

    _tripTimePredictionService.putScheduleDeviationPrediction(tripTimePrediction);
  }

  public ScheduleAdherenceEntries getPredictionsForTripId(
      ServiceDateAndId id) {
    Element element = _tripScheduleAdherenceCache.get(id);
    if (element == null)
      return new ScheduleAdherenceEntries();
    return (ScheduleAdherenceEntries) element.getValue();
  }

  private void setPredictionsForTripId(ServiceDateAndId tripId,
      ScheduleAdherenceEntries predictions) {
    Element element = new Element(tripId, predictions);
    _tripScheduleAdherenceCache.put(element);
  }

}
