package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
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

  private TransitGraphDao _transitGraphDao;

  private StopTimeService _stopTimeService;

  private NarrativeService _narrativeService;

  private TripBeanService _tripBeanService;

  private BlockLocationService _blockLocationService;

  private BlockStatusService _blockStatusService;

  private TripDetailsBeanService _tripDetailsBeanService;

  private ServiceAlertsBeanService _serviceAlertsBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

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
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
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

  @Autowired
  public void setServiceAlertsBeanService(
      ServiceAlertsBeanService serviceAlertsBeanService) {
    _serviceAlertsBeanService = serviceAlertsBeanService;
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

  /****
   * {@link ArrivalsAndDeparturesBeanService} Interface
   ****/

  @Override
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
            applySituationsToBean(time, blockInstance, sti,
                location.getVehicleId(), bean);

            beans.add(bean);
          }
        }

        if (locations.isEmpty()) {

          ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, sti);

          if (sti.getFrequency() == null) {

            /**
             * We don't need to get the scheduled location of a vehicle unless
             * its in our arrival window
             */
            if (isArrivalAndDepartureBeanInRange(bean, from, to)) {

              BlockLocation scheduledLocation = _blockLocationService.getScheduledLocationForBlockInstance(
                  blockInstance, time);
              applyBlockLocationToBean(sti, time, bean, scheduledLocation);
              applySituationsToBean(time, blockInstance, sti, null, bean);

              beans.add(bean);
            }

          } else {
            if (isFrequencyBasedArrivalInRange(sti, fromTime, toTime)) {
              applySituationsToBean(time, blockInstance, sti, null, bean);
              beans.add(bean);
            }
          }
        }
      }
    }

    Collections.sort(beans, new ArrivalAndDepartureComparator());

    return beans;
  }

  @Override
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      AgencyAndId stopId, int stopSequence, AgencyAndId tripId,
      long serviceDate, AgencyAndId vehicleId, long time) {

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);

    if (tripEntry == null)
      return null;

    Map<BlockInstance, List<BlockLocation>> locationsByInstance = _blockStatusService.getBlocks(
        tripEntry.getBlock().getId(), serviceDate, vehicleId, time);

    if (locationsByInstance.isEmpty())
      return null;

    Entry<BlockInstance, List<BlockLocation>> entry = locationsByInstance.entrySet().iterator().next();

    BlockInstance blockInstance = entry.getKey();
    List<BlockLocation> locations = entry.getValue();

    int timeOfServiceDate = (int) ((time - serviceDate) / 1000);
    StopTimeInstance sti = getStopTimeInstance(blockInstance, tripId, stopId,
        stopSequence, serviceDate, timeOfServiceDate);

    ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, sti);

    if (!locations.isEmpty()) {
      /**
       * What if there are multiple locations? Pick the first?
       */
      BlockLocation location = locations.get(0);
      applyBlockLocationToBean(sti, time, bean, location);
      applySituationsToBean(time, blockInstance, sti, location.getVehicleId(),
          bean);
    } else {
      applySituationsToBean(time, blockInstance, sti, null, bean);
    }

    return bean;
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
    pab.setStopSequence(stopTime.getSequence());

    pab.setStatus("default");

    FrequencyEntry frequency = sti.getFrequency();

    if (frequency == null) {
      pab.setScheduledArrivalTime(sti.getArrivalTime());
      pab.setScheduledDepartureTime(sti.getDepartureTime());
      pab.setFrequency(null);
    } else {

      FrequencyBean fb = FrequencyBeanLibrary.getBeanForFrequency(sti.getServiceDate(), frequency);
      pab.setFrequency(fb);

      long t = time + frequency.getHeadwaySecs() * 1000;

      if (t < fb.getStartTime())
        t = fb.getStartTime();
      if (t > fb.getEndTime())
        t = fb.getEndTime();
      pab.setScheduledArrivalTime(t);
      pab.setScheduledDepartureTime(t);
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
      double distanceFromStop = sti.getStopTime().getDistanceAlongBlock()
          - blockLocation.getDistanceAlongBlock();
      bean.setDistanceFromStop(distanceFromStop);
    } else {
      double distanceFromStop = sti.getStopTime().getDistanceAlongBlock()
          - blockLocation.getScheduledDistanceAlongBlock();
      bean.setDistanceFromStop(distanceFromStop);
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

    TripStatusBean tripStatusBean = _tripDetailsBeanService.getBlockLocationAsStatusBean(
        blockLocation, targetTime);
    bean.setTripStatus(tripStatusBean);
  }

  private void applySituationsToBean(long time, BlockInstance blockInstance,
      StopTimeInstance sti, AgencyAndId vehicleId, ArrivalAndDepartureBean bean) {

    List<SituationBean> situations = _serviceAlertsBeanService.getSituationsForStopCall(
        time, blockInstance, sti.getStopTime(), vehicleId);

    if (!situations.isEmpty())
      bean.setSituations(situations);
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

  /**
   * 
   * @param blockInstance
   * @param tripId
   * @param stopId
   * @param stopSequence
   * @param serviceDate TODO
   * @param timeOfServiceDate
   * @return
   */
  private StopTimeInstance getStopTimeInstance(BlockInstance blockInstance,
      AgencyAndId tripId, AgencyAndId stopId, int stopSequence,
      long serviceDate, int timeOfServiceDate) {

    /**
     * Note that this is just a linear search. If this ends up being a
     * performance bottle-neck, we may have to look for a faster method here
     */
    BlockTripEntry blockTrip = getBlockTripEntry(blockInstance.getBlock(),
        tripId);

    if (blockTrip == null)
      return null;

    BlockStopTimeEntry blockStopTime = getBlockStopTime(blockTrip, stopId,
        stopSequence, timeOfServiceDate);
    return new StopTimeInstance(blockStopTime, serviceDate,
        blockInstance.getFrequency());
  }

  private BlockTripEntry getBlockTripEntry(BlockConfigurationEntry blockConfig,
      AgencyAndId tripId) {
    for (BlockTripEntry blockTrip : blockConfig.getTrips()) {
      if (blockTrip.getTrip().getId().equals(tripId))
        return blockTrip;
    }
    return null;
  }

  private BlockStopTimeEntry getBlockStopTime(BlockTripEntry blockTrip,
      AgencyAndId stopId, int stopSequence, int timeOfServiceDate) {

    /**
     * We don't iterate over block stop times directly because there is
     * performance penalty with instantiating each. Also note that this will
     * currently miss the case where a stop is visited twice in the same trip.
     */
    TripEntry trip = blockTrip.getTrip();
    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    if (stopSequence > -1) {

      /**
       * If a stop sequence has been specified, we start our search at the
       * specified index, expanding our search until we find the target stop. We
       * allow this flexibility in the case of a bookmarked arrival-departure
       * where the stop sequence has changed slightly due to the addition or
       * subtraction of a previous stop.
       */
      int offset = 0;
      while (true) {
        int before = stopSequence - offset;
        if (isMatch(stopTimes, stopId, before))
          return blockTrip.getStopTimes().get(before);
        int after = stopSequence + offset;
        if (isMatch(stopTimes, stopId, after))
          return blockTrip.getStopTimes().get(after);

        if (before < 0 && after >= stopTimes.size())
          return null;

        offset++;

      }
    } else {

      Min<BlockStopTimeEntry> m = new Min<BlockStopTimeEntry>();
      int index = 0;

      for (StopTimeEntry stopTime : stopTimes) {
        if (stopTime.getStop().getId().equals(stopId)) {
          int a = Math.abs(timeOfServiceDate - stopTime.getArrivalTime());
          int b = Math.abs(timeOfServiceDate - stopTime.getDepartureTime());
          int delta = Math.min(a, b);
          m.add(delta, blockTrip.getStopTimes().get(index));
        }
        index++;
      }

      if (m.isEmpty())
        return null;

      return m.getMinElement();
    }
  }

  private boolean isMatch(List<StopTimeEntry> stopTimes, AgencyAndId stopId,
      int index) {
    if (index < 0 || index >= stopTimes.size())
      return false;
    StopTimeEntry stopTime = stopTimes.get(index);
    StopEntry stop = stopTime.getStop();
    return stop.getId().equals(stopId);
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
