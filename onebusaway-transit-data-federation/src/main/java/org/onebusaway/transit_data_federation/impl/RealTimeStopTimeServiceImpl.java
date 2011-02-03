package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.RealTimeStopTimeService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
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
import org.springframework.stereotype.Component;

@Component
class RealTimeStopTimeServiceImpl implements RealTimeStopTimeService {

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

  private BlockLocationService _blockLocationService;

  private BlockStatusService _blockStatusService;

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Override
  public List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRange(
      StopEntry stop, long currentTime, long fromTime, long toTime) {

    // We add a buffer before and after to catch late and early buses
    Date fromTimeBuffered = new Date(fromTime - MINUTES_BEFORE_BUFFER * 60
        * 1000);
    Date toTimeBuffered = new Date(toTime + MINUTES_AFTER_BUFFER * 60 * 1000);

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        stop, fromTimeBuffered, toTimeBuffered);

    Map<BlockInstance, List<StopTimeInstance>> stisByBlockId = getStopTimeInstancesByBlockInstance(stis);

    List<ArrivalAndDepartureInstance> instances = new ArrayList<ArrivalAndDepartureInstance>();

    for (Map.Entry<BlockInstance, List<StopTimeInstance>> entry : stisByBlockId.entrySet()) {

      BlockInstance blockInstance = entry.getKey();
      List<BlockLocation> locations = _blockLocationService.getLocationsForBlockInstance(
          blockInstance, currentTime);

      List<StopTimeInstance> stisForBlock = entry.getValue();

      for (StopTimeInstance sti : stisForBlock) {

        for (BlockLocation location : locations) {

          ArrivalAndDepartureInstance instance = getStopTimeInstanceAsArrivalAndDepartureInstance(sti);
          applyBlockLocationToInstance(instance, location, currentTime);

          if (isArrivalAndDepartureBeanInRange(instance, fromTime, toTime))
            instances.add(instance);
        }

        if (locations.isEmpty()) {

          ArrivalAndDepartureInstance instance = getStopTimeInstanceAsArrivalAndDepartureInstance(sti);

          if (sti.getFrequency() == null) {

            /**
             * We don't need to get the scheduled location of a vehicle unless
             * its in our arrival window
             */
            if (isArrivalAndDepartureBeanInRange(instance, fromTime, toTime)) {

              BlockLocation scheduledLocation = _blockLocationService.getScheduledLocationForBlockInstance(
                  blockInstance, currentTime);
              if (scheduledLocation != null)
                applyBlockLocationToInstance(instance, scheduledLocation,
                    currentTime);

              instances.add(instance);
            }

          } else {
            if (isFrequencyBasedArrivalInRange(blockInstance, fromTime, toTime)) {
              instances.add(instance);
            }
          }
        }
      }
    }

    return instances;
  }

  @Override
  public ArrivalAndDepartureInstance getArrivalAndDepartureForStop(
      StopEntry stop, int stopSequence, TripEntry trip, long serviceDate,
      AgencyAndId vehicleId, long time) {

    Map<BlockInstance, List<BlockLocation>> locationsByInstance = _blockStatusService.getBlocks(
        trip.getBlock().getId(), serviceDate, vehicleId, time);

    if (locationsByInstance.isEmpty())
      return null;

    Map.Entry<BlockInstance, List<BlockLocation>> entry = locationsByInstance.entrySet().iterator().next();

    BlockInstance blockInstance = entry.getKey();
    List<BlockLocation> locations = entry.getValue();

    int timeOfServiceDate = (int) ((time - serviceDate) / 1000);

    ArrivalAndDepartureInstance instance = getStopTimeInstance(blockInstance,
        trip.getId(), stop.getId(), stopSequence, serviceDate,
        timeOfServiceDate);

    if (!locations.isEmpty()) {
      /**
       * What if there are multiple locations? Pick the first?
       */
      BlockLocation location = locations.get(0);
      applyBlockLocationToInstance(instance, location, time);
    }

    return instance;
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

  private ArrivalAndDepartureInstance getStopTimeInstanceAsArrivalAndDepartureInstance(
      StopTimeInstance sti) {

    BlockInstance blockInstance = sti.getBlockInstance();
    BlockStopTimeEntry blockStopTime = sti.getStopTime();
    return new ArrivalAndDepartureInstance(blockInstance, blockStopTime);
  }

  private void applyBlockLocationToInstance(
      ArrivalAndDepartureInstance instance, BlockLocation blockLocation,
      long targetTime) {

    instance.setBlockLocation(blockLocation);

    BlockStopTimeEntry destinationStopTime = instance.getBlockStopTime();

    if (blockLocation.isScheduleDeviationSet()
        || blockLocation.areScheduleDeviationsSet()) {

      int scheduleDeviation = getBestScheduleDeviation(instance, blockLocation);
      setPredictedTimesFromScheduleDeviation(targetTime, instance,
          blockLocation, destinationStopTime, scheduleDeviation);
    }
  }

  private int getBestScheduleDeviation(ArrivalAndDepartureInstance instance,
      BlockLocation blockLocation) {

    SortedMap<Integer, Double> scheduleDeviations = blockLocation.getScheduleDeviations();

    if (scheduleDeviations != null && !scheduleDeviations.isEmpty()) {
      return (int) InterpolationLibrary.interpolate(scheduleDeviations,
          instance.getBlockStopTime().getStopTime().getArrivalTime(),
          EOutOfRangeStrategy.LAST_VALUE);
    } else if (blockLocation.isScheduleDeviationSet()) {
      return (int) blockLocation.getScheduleDeviation();
    } else {
      return 0;
    }
  }

  private void setPredictedTimesFromScheduleDeviation(long targetTime,
      ArrivalAndDepartureInstance instance, BlockLocation blockLocation,
      BlockStopTimeEntry destinationStopTime, int scheduleDeviation) {

    int effectiveScheduleTime = (int) (((targetTime - instance.getServiceDate()) / 1000) - scheduleDeviation);

    int arrivalDeviation = calculateArrivalDeviation(
        blockLocation.getNextStop(), destinationStopTime,
        effectiveScheduleTime, scheduleDeviation);

    int departureDeviation = calculateDepartureDeviation(
        blockLocation.getNextStop(), destinationStopTime,
        effectiveScheduleTime, scheduleDeviation);

    long arrivalTime = instance.getScheduledArrivalTime() + arrivalDeviation
        * 1000;
    instance.setPredictedArrivalTime(arrivalTime);

    long departureTime = instance.getScheduledDepartureTime()
        + departureDeviation * 1000;
    instance.setPredictedDepartureTime(departureTime);
  }

  private int calculateArrivalDeviation(BlockStopTimeEntry nextBlockStopTime,
      BlockStopTimeEntry targetBlockStopTime, int effectiveScheduleTime,
      int scheduleDeviation) {

    // TargetStopTime

    if (nextBlockStopTime == null
        || nextBlockStopTime.getBlockSequence() > targetBlockStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    int a = targetBlockStopTime.getAccumulatedSlackTime();
    int b = nextBlockStopTime.getAccumulatedSlackTime();
    double slack = a - b;

    StopTimeEntry nextStopTime = nextBlockStopTime.getStopTime();

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

    // TargetStopTime
    if (nextBlockStopTime == null
        || nextBlockStopTime.getBlockSequence() > targetBlockStopTime.getBlockSequence()) {
      return scheduleDeviation;
    }

    StopTimeEntry nextStopTime = nextBlockStopTime.getStopTime();
    StopTimeEntry targetStopTime = targetBlockStopTime.getStopTime();

    double slack = targetBlockStopTime.getAccumulatedSlackTime()
        - nextBlockStopTime.getAccumulatedSlackTime();

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

  private boolean isArrivalAndDepartureBeanInRange(
      ArrivalAndDepartureInstance instance, long timeFrom, long timeTo) {
    if (timeFrom <= instance.getScheduledArrivalTime()
        && instance.getScheduledArrivalTime() <= timeTo)
      return true;
    if (timeFrom <= instance.getScheduledDepartureTime()
        && instance.getScheduledDepartureTime() <= timeTo)
      return true;
    if (instance.isPredictedArrivalTimeSet()
        && timeFrom <= instance.getPredictedArrivalTime()
        && instance.getPredictedArrivalTime() <= timeTo)
      return true;
    if (instance.isPredictedDepartureTimeSet()
        && timeFrom <= instance.getPredictedDepartureTime()
        && instance.getPredictedDepartureTime() <= timeTo)
      return true;
    return false;
  }

  private boolean isFrequencyBasedArrivalInRange(BlockInstance blockInstance,
      long fromReduced, long toReduced) {
    FrequencyEntry freq = blockInstance.getFrequency();
    long startTime = blockInstance.getServiceDate() + freq.getStartTime()
        * 1000;
    long endTime = blockInstance.getServiceDate() + freq.getEndTime() * 1000;
    return fromReduced <= endTime && startTime <= toReduced;
  }

  private ArrivalAndDepartureInstance getStopTimeInstance(
      BlockInstance blockInstance, AgencyAndId tripId, AgencyAndId stopId,
      int stopSequence, long serviceDate, int timeOfServiceDate) {

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

    return new ArrivalAndDepartureInstance(blockInstance, blockStopTime);
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

}
