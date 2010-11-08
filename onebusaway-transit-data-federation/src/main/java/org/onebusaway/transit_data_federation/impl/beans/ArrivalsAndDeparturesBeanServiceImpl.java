package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.utility.EOutOfRangeStrategy;
import org.onebusaway.utility.InterpolationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.beans:name=ArrivalsAndDeparturesBeanServiceImpl")
public class ArrivalsAndDeparturesBeanServiceImpl implements
    ArrivalsAndDeparturesBeanService {

  /**
   * This let's us capture trips that were scheduled to start 30 minutes before
   * the left-most edge of the user's search window, but that might be running
   * up to 30 minutes late.
   */
  static final int MINUTES_BEFORE_BUFFER = 30;

  /**
   * This let's us capture trips that were scheduled to start 10 minutes after
   * the right-most edge of the user's search window, but that might be running
   * up to 10 minutes early.
   */
  static final int MINUTES_AFTER_BUFFER = 10;

  private StopTimeService _stopTimeService;

  private NarrativeService _narrativeService;

  private TripBeanService _tripBeanService;

  private BlockLocationService _blockLocationService;

  private TripDetailsBeanService _tripDetailsBeanService;

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  @Autowired
  public void setTripDetailsBeanService(
      TripDetailsBeanService tripDetailsBeanService) {
    _tripDetailsBeanService = tripDetailsBeanService;
  }

  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

  @ManagedAttribute()
  public int getStopTimesTotal() {
    return _stopTimesTotal.intValue();
  }

  @ManagedAttribute
  public int getStopTimesWithPredictions() {
    return _stopTimesWithPredictions.intValue();
  }

  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(
      AgencyAndId id, ArrivalsAndDeparturesQueryBean query) {

    long time = query.getTime();

    int minutesBefore = Math.max(query.getMinutesBefore(),
        query.getFrequencyMinutesBefore());
    int minutesAfter = Math.max(query.getMinutesAfter(),
        query.getFrequencyMinutesAfter());

    // We add a buffer before and after to catch late and early buses
    Calendar c = Calendar.getInstance();

    c.setTimeInMillis(time);
    c.add(Calendar.MINUTE, -MINUTES_BEFORE_BUFFER - minutesBefore);
    Date fromTimeBuffered = c.getTime();

    c.setTimeInMillis(time);
    c.add(Calendar.MINUTE, MINUTES_AFTER_BUFFER + minutesAfter);
    Date toTimeBuffered = c.getTime();

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        id, fromTimeBuffered, toTimeBuffered);

    Map<BlockInstance, List<StopTimeInstance>> stisByBlockId = getStopTimeInstancesByBlockInstance(stis);

    List<ArrivalAndDepartureBean> beans = new ArrayList<ArrivalAndDepartureBean>();

    long fromTime = time - query.getMinutesBefore() * 60 * 1000;
    long toTime = time + query.getMinutesAfter() * 60 * 1000;

    long frequencyFromTime = time - query.getFrequencyMinutesBefore() * 60
        * 1000;
    long frequencyToTime = time + query.getFrequencyMinutesAfter() * 60 * 1000;

    for (Map.Entry<BlockInstance, List<StopTimeInstance>> entry : stisByBlockId.entrySet()) {

      BlockInstance blockInstance = entry.getKey();
      List<BlockLocation> locations = _blockLocationService.getLocationsForBlockInstance(
          blockInstance, time);

      List<StopTimeInstance> stisForBlock = entry.getValue();

      for (StopTimeInstance sti : stisForBlock) {

        long from = sti.getFrequency() != null ? frequencyFromTime : fromTime;
        long to = sti.getFrequency() != null ? frequencyToTime : toTime;

        for (BlockLocation location : locations) {

          ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, sti);
          applyBlockLocationToBean(sti, time, bean, location);

          if (isArrivalAndDepartureBeanInRange(bean, from, to)) {
            beans.add(bean);
          }
        }

        if (locations.isEmpty()) {
          ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, sti);
          if (sti.getFrequency() == null) {
            if (isArrivalAndDepartureBeanInRange(bean, from, to))
              beans.add(bean);
          } else {
            if (isFrequencyBasedArrivalInRange(sti, fromTime, toTime))
              beans.add(bean);
          }
        }
      }
    }

    Collections.sort(beans, new ArrivalAndDepartureComparator());

    return beans;
  }

  /****
   * Private Methods
   ****/

  private Map<BlockInstance, List<StopTimeInstance>> getStopTimeInstancesByBlockInstance(
      List<StopTimeInstance> stopTimes) {

    Map<BlockInstance, List<StopTimeInstance>> r = new FactoryMap<BlockInstance, List<StopTimeInstance>>(
        new ArrayList<StopTimeInstance>());

    for (StopTimeInstance stopTime : stopTimes) {
      BlockStopTimeEntry blockStopTime = stopTime.getStopTime();
      BlockTripEntry blockTrip = blockStopTime.getTrip();
      BlockConfigurationEntry blockConfiguration = blockTrip.getBlockConfiguration();
      long serviceDate = stopTime.getServiceDate();
      BlockInstance blockInstance = new BlockInstance(blockConfiguration,
          serviceDate, stopTime.getFrequency());
      r.get(blockInstance).add(stopTime);
    }

    return r;
  }

  private ArrivalAndDepartureBean getStopTimeInstanceAsBean(long time,
      StopTimeInstance sti) {

    ArrivalAndDepartureBean pab = new ArrivalAndDepartureBean();

    pab.setServiceDate(sti.getServiceDate());

    BlockStopTimeEntry blockStopTime = sti.getStopTime();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    StopEntry stop = stopTime.getStop();
    TripEntry trip = stopTime.getTrip();

    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    pab.setTrip(tripBean);

    StopTimeNarrative stopTimeNarrative = _narrativeService.getStopTimeForEntry(stopTime);
    pab.setRouteShortName(stopTimeNarrative.getRouteShortName());
    pab.setTripHeadsign(stopTimeNarrative.getStopHeadsign());

    pab.setStopId(ApplicationBeanLibrary.getId(stop.getId()));

    pab.setStatus("default");

    FrequencyEntry frequency = sti.getFrequency();

    if (frequency == null) {
      pab.setScheduledArrivalTime(sti.getArrivalTime());
      pab.setScheduledDepartureTime(sti.getDepartureTime());
      pab.setFrequency(null);
    } else {
      long t = time + frequency.getHeadwaySecs() * 1000;
      pab.setScheduledArrivalTime(t);
      pab.setScheduledDepartureTime(t);

      FrequencyBean fb = new FrequencyBean();
      fb.setStartTime(sti.getServiceDate() + frequency.getStartTime() * 1000);
      fb.setEndTime(sti.getServiceDate() + frequency.getEndTime() * 1000);
      fb.setHeadway(frequency.getHeadwaySecs());
      pab.setFrequency(fb);
    }

    return pab;
  }

  private void applyBlockLocationToBean(StopTimeInstance sti, long targetTime,
      ArrivalAndDepartureBean bean, BlockLocation blockLocation) {

    BlockStopTimeEntry destinationStopTime = sti.getStopTime();

    if (blockLocation.isScheduleDeviationSet()
        || blockLocation.areScheduleDeviationsSet()) {

      int scheduleDeviation = getBestScheduleDeviation(sti, blockLocation);
      setPredictedTimesFromScheduleDeviation(sti, targetTime, bean,
          blockLocation, destinationStopTime, scheduleDeviation);

      if (sti.getFrequency() != null) {
        bean.setScheduledArrivalTime(bean.getPredictedArrivalTime());
        bean.setScheduledDepartureTime(bean.getPredictedDepartureTime());
      }
    }

    bean.setPredicted(blockLocation.isPredicted());

    // Distance from stop
    if (blockLocation.hasDistanceAlongBlock()) {
      double distanceFromStop = sti.getStopTime().getDistaceAlongBlock()
          - blockLocation.getDistanceAlongBlock();
      bean.setDistanceFromStop(distanceFromStop);
    } else {
      bean.setDistanceFromStop(blockLocation.getScheduledDistanceAlongBlock());
    }

    // Number of stops away
    if (blockLocation.getNextStop() != null) {
      BlockStopTimeEntry nextStopTime = blockLocation.getNextStop();
      bean.setNumberOfStopsAway(destinationStopTime.getBlockSequence()
          - nextStopTime.getBlockSequence());
    }

    if (blockLocation.getLastUpdateTime() > 0)
      bean.setLastUpdateTime(blockLocation.getLastUpdateTime());

    if (blockLocation.getVehicleId() != null)
      bean.setVehicleId(AgencyAndIdLibrary.convertToString(blockLocation.getVehicleId()));

    TripStatusBean tripStatusBean = _tripDetailsBeanService.getBlockLocationAsStatusBean(blockLocation);
    bean.setTripStatus(tripStatusBean);
  }

  private int getBestScheduleDeviation(StopTimeInstance sti,
      BlockLocation blockLocation) {

    SortedMap<Integer, Double> scheduleDeviations = blockLocation.getScheduleDeviations();

    if (scheduleDeviations != null && !scheduleDeviations.isEmpty()) {
      return (int) InterpolationLibrary.interpolate(scheduleDeviations,
          sti.getStopTime().getStopTime().getArrivalTime(),
          EOutOfRangeStrategy.LAST_VALUE);
    } else if (blockLocation.isScheduleDeviationSet()) {
      return (int) blockLocation.getScheduleDeviation();
    } else {
      return 0;
    }
  }

  private void setPredictedTimesFromScheduleDeviation(StopTimeInstance sti,
      long targetTime, ArrivalAndDepartureBean bean,
      BlockLocation blockLocation, BlockStopTimeEntry destinationStopTime,
      int scheduleDeviation) {

    int effectiveScheduleTime = (int) (((targetTime - sti.getServiceDate()) / 1000) - scheduleDeviation);

    int arrivalDeviation = calculateArrivalDeviation(
        blockLocation.getNextStop(), destinationStopTime,
        effectiveScheduleTime, scheduleDeviation);

    int departureDeviation = calculateDepartureDeviation(
        blockLocation.getNextStop(), destinationStopTime,
        effectiveScheduleTime, scheduleDeviation);

    bean.setPredictedArrivalTime(sti.getArrivalTime() + arrivalDeviation * 1000);

    bean.setPredictedDepartureTime(sti.getDepartureTime() + departureDeviation
        * 1000);
  }

  private boolean isArrivalAndDepartureBeanInRange(
      ArrivalAndDepartureBean bean, long timeFrom, long timeTo) {
    if (timeFrom <= bean.getScheduledArrivalTime()
        && bean.getScheduledArrivalTime() <= timeTo)
      return true;
    if (timeFrom <= bean.getScheduledDepartureTime()
        && bean.getScheduledDepartureTime() <= timeTo)
      return true;
    if (bean.hasPredictedArrivalTime()
        && timeFrom <= bean.getPredictedArrivalTime()
        && bean.getPredictedArrivalTime() <= timeTo)
      return true;
    if (bean.hasPredictedDepartureTime()
        && timeFrom <= bean.getPredictedDepartureTime()
        && bean.getPredictedDepartureTime() <= timeTo)
      return true;
    return false;
  }

  private boolean isFrequencyBasedArrivalInRange(StopTimeInstance sti,
      long fromReduced, long toReduced) {
    FrequencyEntry freq = sti.getFrequency();
    long startTime = sti.getServiceDate() + freq.getStartTime() * 1000;
    long endTime = sti.getServiceDate() + freq.getEndTime() * 1000;
    return fromReduced <= endTime && startTime <= toReduced;
  }

  private int calculateArrivalDeviation(BlockStopTimeEntry nextBlockStopTime,
      BlockStopTimeEntry targetBlockStopTime, int effectiveScheduleTime,
      int scheduleDeviation) {

    StopTimeEntry nextStopTime = nextBlockStopTime.getStopTime();

    // TargetStopTime

    if (nextStopTime == null
        || nextBlockStopTime.getBlockSequence() > targetBlockStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    double slack = targetBlockStopTime.getAccumulatedSlackTime()
        - nextStopTime.getAccumulatedSlackTime();

    if (nextStopTime.getArrivalTime() <= effectiveScheduleTime
        && effectiveScheduleTime <= nextStopTime.getDepartureTime()) {
      slack -= (effectiveScheduleTime - nextStopTime.getArrivalTime());
    }

    slack = Math.max(slack, 0);

    if (slack > 0 && scheduleDeviation > 0)
      scheduleDeviation -= Math.min(scheduleDeviation, slack);

    return scheduleDeviation;
  }

  private int calculateDepartureDeviation(BlockStopTimeEntry nextBlockStopTime,
      BlockStopTimeEntry targetBlockStopTime, int effectiveScheduleTime,
      int scheduleDeviation) {

    StopTimeEntry nextStopTime = nextBlockStopTime.getStopTime();
    StopTimeEntry targetStopTime = targetBlockStopTime.getStopTime();

    // TargetStopTime
    if (nextStopTime == null
        || nextBlockStopTime.getBlockSequence() > targetBlockStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    double slack = targetStopTime.getAccumulatedSlackTime()
        - nextStopTime.getAccumulatedSlackTime();

    slack += targetStopTime.getSlackTime();

    if (nextStopTime.getArrivalTime() <= effectiveScheduleTime
        && effectiveScheduleTime <= nextStopTime.getDepartureTime()) {
      slack -= (effectiveScheduleTime - nextStopTime.getArrivalTime());
    }

    slack = Math.max(slack, 0);

    if (slack > 0 && scheduleDeviation > 0)
      scheduleDeviation -= Math.min(scheduleDeviation, slack);

    return scheduleDeviation;
  }

  private static class ArrivalAndDepartureComparator implements
      Comparator<ArrivalAndDepartureBean> {

    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      long t1 = o1.getScheduledArrivalTime();
      if (o1.hasPredictedArrivalTime())
        t1 = o1.getPredictedArrivalTime();
      long t2 = o2.getScheduledArrivalTime();
      if (o2.hasPredictedArrivalTime())
        t2 = o2.getPredictedArrivalTime();
      return (int) (t1 - t2);
    }
  }

}
