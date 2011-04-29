package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import org.onebusaway.collections.Min;
import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.blocks.IndexAdapters;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyStopTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopTimeServiceImpl implements StopTimeService {

  private TransitGraphDao _graph;

  private ExtendedCalendarService _calendarService;

  private BlockIndexService _blockIndexService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Override
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to) {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId, true);
    return getStopTimeInstancesInTimeRange(stopEntry, from, to,
        EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED);
  }

  @Override
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      StopEntry stopEntry, Date from, Date to,
      EFrequencyStopTimeBehavior frequencyBehavior) {

    List<StopTimeInstance> stopTimeInstances = new ArrayList<StopTimeInstance>();

    for (BlockStopTimeIndex index : _blockIndexService.getStopTimeIndicesForStop(stopEntry)) {

      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);

      for (Date serviceDate : serviceDates) {
        getStopTimesForStopAndServiceDateAndTimeRange(index, serviceDate, from,
            to, stopTimeInstances);
      }
    }

    List<FrequencyStopTripIndex> frequencyStopTripIndices = _blockIndexService.getFrequencyStopTripIndicesForStop(stopEntry);

    for (FrequencyStopTripIndex index : frequencyStopTripIndices) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      for (Date serviceDate : serviceDates) {
        getFrequenciesForStopAndServiceIdsAndTimeRange(index, serviceDate,
            from, to, stopTimeInstances, frequencyBehavior);
      }
    }

    return stopTimeInstances;
  }

  @Override
  public Range getDepartureForStopAndServiceDate(AgencyAndId stopId,
      ServiceDate serviceDate) {

    StopEntry stop = _graph.getStopEntryForId(stopId, true);

    List<BlockStopTimeIndex> indices = _blockIndexService.getStopTimeIndicesForStop(stop);

    Range interval = new Range();

    for (BlockStopTimeIndex index : indices) {
      extendIntervalWithIndex(serviceDate, interval, index);
    }

    List<FrequencyBlockStopTimeIndex> freqIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stop);

    for (FrequencyBlockStopTimeIndex index : freqIndices)
      extendIntervalWithIndex(serviceDate, interval, index);

    return interval;
  }

  @Override
  public List<StopTimeInstance> getNextBlockSequenceDeparturesForStop(
      StopEntry stopEntry, long time) {

    List<StopTimeInstance> stopTimeInstances = new ArrayList<StopTimeInstance>();

    List<BlockStopSequenceIndex> blockStopTripIndices = _blockIndexService.getStopSequenceIndicesForStop(stopEntry);

    for (BlockStopSequenceIndex index : blockStopTripIndices) {

      List<Date> serviceDates = _calendarService.getNextServiceDatesForDepartureInterval(
          index.getServiceIds(), index.getServiceInterval(), time);

      for (Date serviceDate : serviceDates) {

        int relativeFrom = effectiveTime(serviceDate.getTime(), time);

        int fromIndex = GenericBinarySearch.search(index, index.size(),
            relativeFrom, IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE);

        if (fromIndex < index.size()) {
          BlockStopTimeEntry blockStopTime = index.getBlockStopTimeForIndex(fromIndex);
          StopTimeInstance sti = new StopTimeInstance(blockStopTime,
              serviceDate);
          stopTimeInstances.add(sti);
        }
      }
    }

    List<FrequencyBlockStopTimeIndex> frequencyIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry);

    for (FrequencyBlockStopTimeIndex index : frequencyIndices) {

      List<Date> serviceDates = _calendarService.getNextServiceDatesForDepartureInterval(
          index.getServiceIds(), index.getServiceInterval(), time);

      for (Date serviceDate : serviceDates) {

        int relativeFrom = effectiveTime(serviceDate.getTime(), time);

        int fromIndex = GenericBinarySearch.search(index, index.size(),
            relativeFrom, IndexAdapters.FREQUENCY_END_TIME_INSTANCE);

        List<FrequencyBlockStopTimeEntry> frequencyStopTimes = index.getFrequencyStopTimes();
        if (fromIndex < index.size()) {
          FrequencyBlockStopTimeEntry entry = frequencyStopTimes.get(fromIndex);
          BlockStopTimeEntry bst = entry.getStopTime();
          FrequencyEntry frequency = entry.getFrequency();
          int stopTimeOffset = entry.getStopTimeOffset();

          int tFrom = Math.max(relativeFrom, frequency.getStartTime());

          tFrom = snapToFrequencyStopTime(frequency, tFrom, stopTimeOffset,
              true);

          int frequencyOffset = tFrom - bst.getStopTime().getDepartureTime();
          StopTimeInstance sti = new StopTimeInstance(bst,
              serviceDate.getTime(), frequency, frequencyOffset);
          stopTimeInstances.add(sti);
        }
      }

    }
    return stopTimeInstances;

  }

  @Override
  public List<Pair<StopTimeInstance>> getDeparturesBetweenStopPairInTimeRange(
      StopEntry fromStop, StopEntry toStop, Date fromDepartureTime,
      Date toDepartureTime) {

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();

    getDeparturesBetweenStopPairInTimeRange(fromStop, toStop,
        fromDepartureTime, toDepartureTime, results);

    getFrequencyDeparturesBetweenStopPairInTimeRange(fromStop, toStop,
        fromDepartureTime, toDepartureTime, results);

    return results;
  }

  @Override
  public List<Pair<StopTimeInstance>> getArrivalsBetweenStopPairInTimeRange(
      StopEntry fromStop, StopEntry toStop, Date fromArrivalTime,
      Date toArrivalTime) {

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();

    getArrivalsBetweenStopPairInTimeRange(fromStop, toStop, fromArrivalTime,
        toArrivalTime, results);

    getFrequencyArrivalsBetweenStopPairInTimeRange(fromStop, toStop,
        fromArrivalTime, toArrivalTime, results);

    return results;
  }

  @Override
  public List<Pair<StopTimeInstance>> getNextDeparturesBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date fromTime, int lookBehind,
      int lookAhead, int resultCount) {

    if (resultCount == 0)
      return Collections.emptyList();

    List<Pair<BlockStopSequenceIndex>> indexPairs = _blockIndexService.getBlockSequenceIndicesBetweenStops(
        fromStop, toStop);

    long targetTime = fromTime.getTime() - lookBehind * 1000;

    PriorityQueue<Pair<StopTimeInstance>> queue = new PriorityQueue<Pair<StopTimeInstance>>(
        resultCount, new WorstArrivalTimeComparator());

    for (Pair<BlockStopSequenceIndex> pair : indexPairs) {

      BlockStopSequenceIndex fromStopIndex = pair.getFirst();
      BlockStopSequenceIndex toStopIndex = pair.getSecond();

      List<BlockStopTimeEntry> toStopTimes = toStopIndex.getStopTimes();

      List<Date> serviceDates = _calendarService.getNextServiceDatesForDepartureInterval(
          fromStopIndex.getServiceIds(), fromStopIndex.getServiceInterval(),
          targetTime);

      for (Date serviceDate : serviceDates) {

        if (queue.size() == resultCount) {
          Pair<StopTimeInstance> stiPair = queue.peek();
          if (stiPair.getSecond().getArrivalTime() < serviceDate.getTime())
            break;
        }

        int relativeFrom = effectiveTime(serviceDate.getTime(),
            fromTime.getTime());

        int fromIndex = GenericBinarySearch.search(fromStopIndex,
            fromStopIndex.size(), relativeFrom,
            IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE);

        int hits = 0;

        while (fromIndex < fromStopIndex.size() && hits < resultCount) {
          BlockStopTimeEntry blockStopTime = fromStopIndex.getBlockStopTimeForIndex(fromIndex);
          StopTimeInstance stiFrom = new StopTimeInstance(blockStopTime,
              serviceDate);
          BlockStopTimeEntry stopTimeTo = toStopTimes.get(fromIndex);
          StopTimeInstance stiTo = new StopTimeInstance(stopTimeTo, serviceDate);
          
          if (queue.size() == resultCount) {
            Pair<StopTimeInstance> stiPair = queue.peek();
            if (stiPair.getSecond().getArrivalTime() < stiTo.getArrivalTime())
              break;
          }
          
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          queue.add(stiPair);
          
          while (queue.size() > resultCount)
            queue.poll();
          
          fromIndex++;
        }
      }
    }

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();
    results.addAll(queue);

    return results;
  }

  @Override
  public List<Pair<StopTimeInstance>> getPreviousArrivalsBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date toTime,
      boolean includeAllSequences) {

    List<Pair<BlockStopSequenceIndex>> indexPairs = _blockIndexService.getBlockSequenceIndicesBetweenStops(
        fromStop, toStop);

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();
    Min<Pair<StopTimeInstance>> min = new Min<Pair<StopTimeInstance>>();

    for (Pair<BlockStopSequenceIndex> pair : indexPairs) {

      BlockStopSequenceIndex fromStopIndex = pair.getFirst();
      BlockStopSequenceIndex toStopIndex = pair.getSecond();

      List<BlockStopTimeEntry> fromStopTimes = fromStopIndex.getStopTimes();

      List<Date> serviceDates = _calendarService.getPreviousServiceDatesForArrivalInterval(
          toStopIndex.getServiceIds(), toStopIndex.getServiceInterval(),
          toTime.getTime());

      for (Date serviceDate : serviceDates) {

        int relativeTo = effectiveTime(serviceDate.getTime(), toTime.getTime());

        int toIndex = GenericBinarySearch.search(toStopIndex,
            toStopIndex.size(), relativeTo,
            IndexAdapters.BLOCK_STOP_TIME_ARRIVAL_INSTANCE);

        toIndex--;

        if (0 <= toIndex) {
          BlockStopTimeEntry blockStopTime = toStopIndex.getBlockStopTimeForIndex(toIndex);
          StopTimeInstance stiTo = new StopTimeInstance(blockStopTime,
              serviceDate);
          BlockStopTimeEntry stopTimeFrom = fromStopTimes.get(toIndex);
          StopTimeInstance stiFrom = new StopTimeInstance(stopTimeFrom,
              serviceDate);
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          if (includeAllSequences) {
            results.add(stiPair);
          } else {
            /**
             * Note that we use the negative arrival time here because we
             * actually want the arrival with the max arrival time
             */
            min.add(-stiTo.getArrivalTime(), stiPair);
          }
        }
      }
    }

    if (includeAllSequences)
      return results;
    else
      return min.getMinElements();
  }

  /****
   * Private Methods
   ****/

  private void getDeparturesBetweenStopPairInTimeRange(StopEntry fromStop,
      StopEntry toStop, Date fromDepartureTime, Date toDepartureTime,
      List<Pair<StopTimeInstance>> results) {

    /**
     * First find indices shared between the two stops
     */
    List<Pair<BlockStopSequenceIndex>> indexPairs = _blockIndexService.getBlockSequenceIndicesBetweenStops(
        fromStop, toStop);

    for (Pair<BlockStopSequenceIndex> pair : indexPairs) {

      BlockStopSequenceIndex fromStopIndex = pair.getFirst();
      BlockStopSequenceIndex toStopIndex = pair.getSecond();

      List<BlockStopTimeEntry> toStopTimes = toStopIndex.getStopTimes();

      ServiceIdActivation serviceIds = fromStopIndex.getServiceIds();
      ServiceInterval interval = fromStopIndex.getServiceInterval();

      /**
       * Find applicable service dates for the departure stop and index
       */
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          serviceIds, interval, fromDepartureTime, toDepartureTime);

      for (Date serviceDate : serviceDates) {

        List<StopTimeInstance> instances = new ArrayList<StopTimeInstance>();

        /**
         * Find departures within the specified range on the specified service
         * date
         */
        int indexOffset = getStopTimesForStopAndServiceDateAndTimeRange(
            fromStopIndex, serviceDate, fromDepartureTime, toDepartureTime,
            instances);

        /**
         * For each departure, we need to find the corresponding arrival
         */
        for (int i = 0; i < instances.size(); i++) {
          StopTimeInstance stiFrom = instances.get(i);
          BlockStopTimeEntry stopTimeTo = toStopTimes.get(indexOffset + i);
          StopTimeInstance stiTo = new StopTimeInstance(stopTimeTo, serviceDate);
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          results.add(stiPair);
        }
      }
    }
  }

  private void getFrequencyDeparturesBetweenStopPairInTimeRange(
      StopEntry fromStop, StopEntry toStop, Date fromDepartureTime,
      Date toDepartureTime, List<Pair<StopTimeInstance>> results) {

    /**
     * First find indices shared between the two stops
     */
    List<Pair<FrequencyStopTripIndex>> indexPairs = _blockIndexService.getFrequencyIndicesBetweenStops(
        fromStop, toStop);

    for (Pair<FrequencyStopTripIndex> pair : indexPairs) {

      FrequencyStopTripIndex fromStopIndex = pair.getFirst();
      FrequencyStopTripIndex toStopIndex = pair.getSecond();

      ServiceIdActivation serviceIds = fromStopIndex.getServiceIds();
      ServiceInterval interval = fromStopIndex.getServiceInterval();

      List<FrequencyBlockStopTimeEntry> toStopTimes = toStopIndex.getFrequencyStopTimes();

      /**
       * Find applicable service dates for the departure stop and index
       */
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          serviceIds, interval, fromDepartureTime, toDepartureTime);

      for (Date serviceDate : serviceDates) {

        List<StopTimeInstance> instances = new ArrayList<StopTimeInstance>();

        /**
         * Find departures within the specified range on the specified service
         * date
         */
        List<Integer> offsets = getFrequenciesForStopAndServiceIdsAndTimeRange(
            fromStopIndex, serviceDate, fromDepartureTime, toDepartureTime,
            instances, EFrequencyStopTimeBehavior.INCLUDE_INTERPOLATED);

        /**
         * For each departure, we need to find the corresponding arrival
         */
        for (int i = 0; i < instances.size(); i++) {
          StopTimeInstance stiFrom = instances.get(i);
          int offset = offsets.get(i);
          FrequencyBlockStopTimeEntry stopTimeTo = toStopTimes.get(offset);
          StopTimeInstance stiTo = new StopTimeInstance(
              stopTimeTo.getStopTime(), serviceDate.getTime(),
              stiFrom.getFrequency(), stiFrom.getFrequencyOffset());
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          results.add(stiPair);
        }
      }
    }
  }

  private void getArrivalsBetweenStopPairInTimeRange(StopEntry fromStop,
      StopEntry toStop, Date fromArrivalTime, Date toArrivalTime,
      List<Pair<StopTimeInstance>> results) {

    /**
     * First find indices shared between the two stops
     */
    List<Pair<BlockStopSequenceIndex>> indexPairs = _blockIndexService.getBlockSequenceIndicesBetweenStops(
        fromStop, toStop);

    for (Pair<BlockStopSequenceIndex> pair : indexPairs) {

      BlockStopSequenceIndex fromStopIndex = pair.getFirst();
      BlockStopSequenceIndex toStopIndex = pair.getSecond();

      List<BlockStopTimeEntry> fromStopTimes = fromStopIndex.getStopTimes();

      ServiceIdActivation serviceIds = toStopIndex.getServiceIds();
      ServiceInterval interval = toStopIndex.getServiceInterval();

      /**
       * Find applicable service dates for the arrival stop and index
       */
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          serviceIds, interval, fromArrivalTime, toArrivalTime);

      for (Date serviceDate : serviceDates) {

        List<StopTimeInstance> instances = new ArrayList<StopTimeInstance>();

        /**
         * Find arrivals within the specified range on the specified service
         * date
         */
        int toIndex = getStopTimesForStopAndServiceDateAndTimeRange(
            toStopIndex, serviceDate, fromArrivalTime, toArrivalTime, instances);

        /**
         * For each arrival, we need to find the corresponding departure
         */
        for (int i = 0; i < instances.size(); i++) {
          StopTimeInstance stiTo = instances.get(i);
          BlockStopTimeEntry stopTimeFrom = fromStopTimes.get(toIndex + i);
          StopTimeInstance stiFrom = new StopTimeInstance(stopTimeFrom,
              serviceDate);
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          results.add(stiPair);
        }
      }
    }
  }

  private void getFrequencyArrivalsBetweenStopPairInTimeRange(
      StopEntry fromStop, StopEntry toStop, Date fromArrivalTime,
      Date toArrivalTime, List<Pair<StopTimeInstance>> results) {

    /**
     * First find indices shared between the two stops
     */
    List<Pair<FrequencyStopTripIndex>> indexPairs = _blockIndexService.getFrequencyIndicesBetweenStops(
        fromStop, toStop);

    for (Pair<FrequencyStopTripIndex> pair : indexPairs) {

      FrequencyStopTripIndex fromStopIndex = pair.getFirst();
      FrequencyStopTripIndex toStopIndex = pair.getSecond();

      List<FrequencyBlockStopTimeEntry> fromStopTimes = fromStopIndex.getFrequencyStopTimes();

      ServiceIdActivation serviceIds = toStopIndex.getServiceIds();
      ServiceInterval interval = toStopIndex.getServiceInterval();

      /**
       * Find applicable service dates for the arrival stop and index
       */
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          serviceIds, interval, fromArrivalTime, toArrivalTime);

      for (Date serviceDate : serviceDates) {

        List<StopTimeInstance> instances = new ArrayList<StopTimeInstance>();

        /**
         * Find arrivals within the specified range on the specified service
         * date
         */
        List<Integer> offsets = getFrequenciesForStopAndServiceIdsAndTimeRange(
            toStopIndex, serviceDate, fromArrivalTime, toArrivalTime,
            instances, EFrequencyStopTimeBehavior.INCLUDE_INTERPOLATED);

        /**
         * For each arrival, we need to find the corresponding departure
         */
        for (int i = 0; i < instances.size(); i++) {
          StopTimeInstance stiTo = instances.get(i);
          int offset = offsets.get(i);
          FrequencyBlockStopTimeEntry stopTimeFrom = fromStopTimes.get(offset);
          StopTimeInstance stiFrom = new StopTimeInstance(
              stopTimeFrom.getStopTime(), serviceDate.getTime(),
              stiTo.getFrequency(), stiTo.getFrequencyOffset());
          Pair<StopTimeInstance> stiPair = Tuples.pair(stiFrom, stiTo);
          results.add(stiPair);
        }
      }
    }
  }

  private int getStopTimesForStopAndServiceDateAndTimeRange(
      HasIndexedBlockStopTimes index, Date serviceDate, Date from, Date to,
      List<StopTimeInstance> instances) {

    List<BlockStopTimeEntry> blockStopTimes = index.getStopTimes();

    int relativeFrom = effectiveTime(serviceDate, from);
    int relativeTo = effectiveTime(serviceDate, to);

    int fromIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
        relativeFrom, IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE);
    int toIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
        relativeTo, IndexAdapters.BLOCK_STOP_TIME_ARRIVAL_INSTANCE);

    for (int in = fromIndex; in < toIndex; in++) {
      BlockStopTimeEntry blockStopTime = blockStopTimes.get(in);
      instances.add(new StopTimeInstance(blockStopTime, serviceDate));
    }

    return fromIndex;
  }

  private List<Integer> getFrequenciesForStopAndServiceIdsAndTimeRange(
      FrequencyStopTripIndex index, Date serviceDate, Date from, Date to,
      List<StopTimeInstance> stopTimeInstances,
      EFrequencyStopTimeBehavior frequencyBehavior) {

    int relativeFrom = effectiveTime(serviceDate, from);
    int relativeTo = effectiveTime(serviceDate, to);

    int fromIndex = GenericBinarySearch.search(index, index.size(),
        relativeFrom, IndexAdapters.FREQUENCY_END_TIME_INSTANCE);
    int toIndex = GenericBinarySearch.search(index, index.size(), relativeTo,
        IndexAdapters.FREQUENCY_START_TIME_INSTANCE);

    List<FrequencyBlockStopTimeEntry> frequencyStopTimes = index.getFrequencyStopTimes();

    List<Integer> offsetsIntoIndex = new ArrayList<Integer>();

    for (int in = fromIndex; in < toIndex; in++) {

      FrequencyBlockStopTimeEntry entry = frequencyStopTimes.get(in);
      BlockStopTimeEntry bst = entry.getStopTime();
      FrequencyEntry frequency = entry.getFrequency();

      switch (frequencyBehavior) {

        case INCLUDE_UNSPECIFIED:
          stopTimeInstances.add(new StopTimeInstance(bst,
              serviceDate.getTime(), frequency));
          offsetsIntoIndex.add(in);
          break;

        case INCLUDE_INTERPOLATED:

          int stopTimeOffset = entry.getStopTimeOffset();

          int tFrom = Math.max(relativeFrom, frequency.getStartTime());
          int tTo = Math.min(relativeTo, frequency.getEndTime());

          tFrom = snapToFrequencyStopTime(frequency, tFrom, stopTimeOffset,
              true);
          tTo = snapToFrequencyStopTime(frequency, tTo, stopTimeOffset, false);

          for (int t = tFrom; t <= tTo; t += frequency.getHeadwaySecs()) {
            int frequencyOffset = t - bst.getStopTime().getDepartureTime();
            stopTimeInstances.add(new StopTimeInstance(bst,
                serviceDate.getTime(), frequency, frequencyOffset));
            offsetsIntoIndex.add(in);
          }
          break;
      }
    }

    return offsetsIntoIndex;
  }

  private int snapToFrequencyStopTime(FrequencyEntry frequency, int timeToSnap,
      int stopTimeOffset, boolean isLowerBound) {
    int offset = timeToSnap - frequency.getStartTime();
    int headway = frequency.getHeadwaySecs();
    int snappedToHeadway = (offset / headway) * headway;
    int snapped = snappedToHeadway + frequency.getStartTime() + stopTimeOffset;
    if (isLowerBound) {
      if (snapped < timeToSnap)
        snapped += headway;
    } else {
      if (snapped > timeToSnap)
        snapped -= headway;
    }
    return snapped;
  }

  private void extendIntervalWithIndex(ServiceDate serviceDate, Range interval,
      AbstractBlockStopTimeIndex index) {
    ServiceIdActivation serviceIds = index.getServiceIds();
    Date date = serviceDate.getAsDate(serviceIds.getTimeZone());
    if (_calendarService.areServiceIdsActiveOnServiceDate(serviceIds, date)) {
      ServiceInterval in = index.getServiceInterval();
      long tFrom = date.getTime() + in.getMinDeparture() * 1000;
      long tTo = date.getTime() + in.getMaxDeparture() * 1000;
      interval.addValue(tFrom);
      interval.addValue(tTo);
    }
  }

  private static final int effectiveTime(Date serviceDate, Date targetTime) {
    return effectiveTime(serviceDate.getTime(), targetTime.getTime());
  }

  private static final int effectiveTime(long serviceDate, long targetTime) {
    return (int) ((targetTime - serviceDate) / 1000);
  }

  private static class WorstArrivalTimeComparator implements
      Comparator<Pair<StopTimeInstance>> {

    @Override
    public int compare(Pair<StopTimeInstance> o1, Pair<StopTimeInstance> o2) {
      long t1 = o1.getSecond().getArrivalTime();
      long t2 = o2.getSecond().getArrivalTime();
      return t1 == t2 ? 0 : (t1 < t2 ? 1 : -1);
    }
  }
}
