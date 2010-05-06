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
@ManagedResource("org.onebusaway.transit_data_federation.impl.predictions:name=BlockScheduleAdherenceStopTimePredictionServiceImpl")
public class BlockScheduleAdherenceStopTimePredictionServiceImpl implements
    StopTimePredictionService, ScheduleAdherenceListener {

  static final long PRUNE_STOP_TIME_PREDICTIONS_THRESHOLD = 5 * 60 * 1000;

  private Cache _blockScheduleAdherenceCache;

  private TripTimePredictionService _tripTimePredictionService;

  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

  public void setTripStopTimePredictionsCache(Cache cache) {
    _blockScheduleAdherenceCache = cache;
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

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> stisByBlockId = getStopTimeInstancesByBlockId(stopTimes);

    for (Map.Entry<ServiceDateAndId, List<StopTimeInstanceProxy>> entry : stisByBlockId.entrySet()) {
      ServiceDateAndId blockId = entry.getKey();
      ScheduleAdherenceEntries entries = getPredictionsForBlockId(blockId);
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

    Map<ServiceDateAndId, ScheduleAdherenceRecord> recordsByBlockId = getScheduleAdherenceRecordsByBlockIdInstance(records);

    for (Map.Entry<ServiceDateAndId, ScheduleAdherenceRecord> entry : recordsByBlockId.entrySet()) {

      ServiceDateAndId blockId = entry.getKey();
      ScheduleAdherenceRecord record = entry.getValue();

      ScheduleAdherenceEntries entries = getPredictionsForBlockId(blockId);
      entries.addPredictions(record);
      setPredictionsForBlockId(blockId, entries);
      recordTripTimePrediction(blockId, record);
    }
  }

  /****
   * Private Methods
   ****/

  private Map<ServiceDateAndId, List<StopTimeInstanceProxy>> getStopTimeInstancesByBlockId(
      List<StopTimeInstanceProxy> stopTimes) {

    Map<ServiceDateAndId, List<StopTimeInstanceProxy>> r = new FactoryMap<ServiceDateAndId, List<StopTimeInstanceProxy>>(
        new ArrayList<StopTimeInstanceProxy>());

    for (StopTimeInstanceProxy stopTime : stopTimes) {
      long serviceDate = stopTime.getServiceDate();
      TripEntry trip = stopTime.getTrip();
      AgencyAndId blockId = trip.getBlockId();
      ServiceDateAndId id = new ServiceDateAndId(serviceDate, blockId);
      r.get(id).add(stopTime);
    }
    return r;
  }

  private Map<ServiceDateAndId, ScheduleAdherenceRecord> getScheduleAdherenceRecordsByBlockIdInstance(
      List<ScheduleAdherenceRecord> records) {

    Map<ServiceDateAndId, ScheduleAdherenceRecord> r = new HashMap<ServiceDateAndId, ScheduleAdherenceRecord>();

    for (ScheduleAdherenceRecord record : records) {
      ServiceDateAndId id = new ServiceDateAndId(record.getServiceDate(),
          record.getBlockId());
      r.put(id, record);
    }

    return r;
  }

  private void recordTripTimePrediction(ServiceDateAndId id,
      ScheduleAdherenceRecord record) {

    if (_tripTimePredictionService == null)
      return;

    long timeOfPrediction = record.getCurrentTime();
    int offset = record.getScheduleDeviation();

    TripTimePrediction tripTimePrediction = new TripTimePrediction(id.getId(),
        id.getServiceDate(), timeOfPrediction, offset, record.getVehicleId());

    _tripTimePredictionService.putScheduleDeviationPrediction(tripTimePrediction);
  }

  public ScheduleAdherenceEntries getPredictionsForBlockId(
      ServiceDateAndId id) {
    Element element = _blockScheduleAdherenceCache.get(id);
    if (element == null)
      return new ScheduleAdherenceEntries();
    return (ScheduleAdherenceEntries) element.getValue();
  }

  private void setPredictionsForBlockId(ServiceDateAndId blockId,
      ScheduleAdherenceEntries predictions) {
    Element element = new Element(blockId, predictions);
    _blockScheduleAdherenceCache.put(element);
  }

}
