package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.blocks.IndexAdapters;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyStopTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;
import org.onebusaway.transit_data_federation.services.blocks.HasIndexedFrequencyBlockTrips;
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

  private static final FirstDepartureTimeComparator _firstDepartureComparator = new FirstDepartureTimeComparator();

  private static final LastArrivalTimeComparator _lastArrivalComparator = new LastArrivalTimeComparator();

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
      StopEntry stopEntry, long time, boolean includePrivateSerivce) {

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

          int frequencyOffset = computeFrequencyOffset(relativeFrom, bst,
              frequency, stopTimeOffset, true);
          StopTimeInstance sti = new StopTimeInstance(bst,
              serviceDate.getTime(), frequency, frequencyOffset);
          stopTimeInstances.add(sti);
        }
      }

    }
    return stopTimeInstances;

  }

  @Override
  public List<Pair<StopTimeInstance>> getNextDeparturesBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date fromTime,
      int runningEarlySlack, int runningLateSlack, int resultCount,
      boolean includePrivateService) {

    if (resultCount == 0)
      return Collections.emptyList();

    PriorityQueue<Pair<StopTimeInstance>> nBestQueue = new PriorityQueue<Pair<StopTimeInstance>>(
        resultCount, _lastArrivalComparator);
    PriorityQueue<Pair<StopTimeInstance>> resultQueue = new PriorityQueue<Pair<StopTimeInstance>>(
        resultCount, _lastArrivalComparator);

    getDeparturesAndArrivalsBetweenStopPair(fromStop, toStop, fromTime,
        runningEarlySlack, runningLateSlack, resultCount, nBestQueue,
        resultQueue, true, includePrivateService);

    getFrequencyDeparturesAndArrivalsBetweenStopPair(fromStop, toStop,
        fromTime, runningEarlySlack, runningLateSlack, resultCount, nBestQueue,
        resultQueue, true);

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();
    results.addAll(resultQueue);
    Collections.sort(results, _firstDepartureComparator);
    return results;
  }

  @Override
  public List<Pair<StopTimeInstance>> getPreviousArrivalsBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date toTime, int runningEarlySlack,
      int runningLateSlack, int resultCount, boolean includePrivateService) {

    if (resultCount == 0)
      return Collections.emptyList();

    PriorityQueue<Pair<StopTimeInstance>> nBestQueue = new PriorityQueue<Pair<StopTimeInstance>>(
        resultCount, _firstDepartureComparator);
    PriorityQueue<Pair<StopTimeInstance>> resultQueue = new PriorityQueue<Pair<StopTimeInstance>>(
        resultCount, _firstDepartureComparator);

    getDeparturesAndArrivalsBetweenStopPair(fromStop, toStop, toTime,
        runningEarlySlack, runningLateSlack, resultCount, nBestQueue,
        resultQueue, false, includePrivateService);

    getFrequencyDeparturesAndArrivalsBetweenStopPair(fromStop, toStop, toTime,
        runningEarlySlack, runningLateSlack, resultCount, nBestQueue,
        resultQueue, false);

    List<Pair<StopTimeInstance>> results = new ArrayList<Pair<StopTimeInstance>>();
    results.addAll(resultQueue);
    Collections.sort(results, _lastArrivalComparator);
    return results;
  }

  /****
   * Private Methods
   * 
   * @param includePrivateService TODO
   ****/

  private void getDeparturesAndArrivalsBetweenStopPair(StopEntry fromStop,
      StopEntry toStop, Date tTime, int runningEarlySlack,
      int runningLateSlack, int resultCount,
      PriorityQueue<Pair<StopTimeInstance>> nBestQueue,
      PriorityQueue<Pair<StopTimeInstance>> resultQueue,
      boolean findDepartures, boolean includePrivateService) {

    List<Pair<BlockStopSequenceIndex>> indexPairs = _blockIndexService.getBlockSequenceIndicesBetweenStops(
        fromStop, toStop);

    long targetTime = tTime.getTime();
    long slackAdjustedTime = targetTime;

    if (findDepartures)
      slackAdjustedTime -= runningLateSlack * 1000;
    else
      slackAdjustedTime += runningEarlySlack * 1000;

    long slack = (runningLateSlack + runningEarlySlack) * 1000;

    for (Pair<BlockStopSequenceIndex> pair : indexPairs) {

      BlockStopSequenceIndex sourceStopIndex = findDepartures ? pair.getFirst()
          : pair.getSecond();
      BlockStopSequenceIndex destStopIndex = findDepartures ? pair.getSecond()
          : pair.getFirst();

      if (!includePrivateService
          && sourceStopIndex.getIndex().isPrivateService())
        continue;

      List<BlockStopTimeEntry> destStopTimes = destStopIndex.getStopTimes();

      List<Date> serviceDates = _calendarService.getServiceDatesForInterval(
          sourceStopIndex.getServiceIds(),
          sourceStopIndex.getServiceInterval(), slackAdjustedTime,
          findDepartures);

      for (Date serviceDate : serviceDates) {

        ServiceInterval destServiceInterval = destStopIndex.getServiceInterval();

        if (serviceDateIsBeyondRangeOfQueue(nBestQueue, serviceDate,
            destServiceInterval, resultCount, findDepartures, slack)) {

          /**
           * The service date is beyond our worst departure-arrival, so we break
           */
          break;
        }

        int relativeTime = effectiveTime(serviceDate.getTime(),
            slackAdjustedTime);

        IndexAdapter<HasIndexedBlockStopTimes> adapter = findDepartures
            ? IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE
            : IndexAdapters.BLOCK_STOP_TIME_ARRIVAL_INSTANCE;

        int sourceStopIndexSize = sourceStopIndex.size();

        int sourceIndex = GenericBinarySearch.search(sourceStopIndex,
            sourceStopIndexSize, relativeTime, adapter);

        /**
         * When searching for arrival times, the index is an upper bound, so we
         * have to decrement to find the first good stop index
         */
        if (!findDepartures)
          sourceIndex--;

        while (0 <= sourceIndex && sourceIndex < sourceStopIndexSize) {

          BlockStopTimeEntry stopTimeSource = sourceStopIndex.getBlockStopTimeForIndex(sourceIndex);
          StopTimeInstance stiSource = new StopTimeInstance(stopTimeSource,
              serviceDate);
          stiSource.setBlockSequence(sourceStopIndex.getBlockSequenceForIndex(sourceIndex));

          BlockStopTimeEntry stopTimeDest = destStopTimes.get(sourceIndex);
          StopTimeInstance stiDest = new StopTimeInstance(stopTimeDest,
              serviceDate);
          stiDest.setBlockSequence(destStopIndex.getBlockSequenceForIndex(sourceIndex));

          if (stopTimeIsBeyondRangeOfQueue(nBestQueue, stiDest, resultCount,
              findDepartures, slack)) {
            break;
          }

          Pair<StopTimeInstance> stiPair = findDepartures ? Tuples.pair(
              stiSource, stiDest) : Tuples.pair(stiDest, stiSource);

          /**
           * We only add to the n-best queue if the arrival-departure is beyond
           * the target time, as opposed to time adjusted by slack
           */
          if (isStopTimeInstanceBeyondTargetTime(stiSource, targetTime,
              findDepartures)) {
            nBestQueue.add(stiPair);
          }

          while (nBestQueue.size() > resultCount)
            nBestQueue.poll();

          /**
           * We always add to the result queue
           */
          resultQueue.add(stiPair);

          while (!resultQueue.isEmpty()) {
            Pair<StopTimeInstance> r = resultQueue.peek();
            StopTimeInstance sti = findDepartures ? r.getSecond()
                : r.getFirst();
            if (stopTimeIsBeyondRangeOfQueue(nBestQueue, sti, resultCount,
                findDepartures, slack))
              resultQueue.poll();
            else
              break;
          }

          if (findDepartures)
            sourceIndex++;
          else
            sourceIndex--;
        }
      }
    }
  }

  private void getFrequencyDeparturesAndArrivalsBetweenStopPair(
      StopEntry fromStop, StopEntry toStop, Date tTime, int runningEarlySlack,
      int runningLateSlack, int resultCount,
      PriorityQueue<Pair<StopTimeInstance>> nBestQueue,
      PriorityQueue<Pair<StopTimeInstance>> resultQueue, boolean findDepartures) {

    List<Pair<FrequencyStopTripIndex>> indexPairs = _blockIndexService.getFrequencyIndicesBetweenStops(
        fromStop, toStop);

    long targetTime = tTime.getTime();
    long slackAdjustedTime = targetTime;

    if (findDepartures)
      slackAdjustedTime -= runningEarlySlack * 1000;
    else
      slackAdjustedTime += runningLateSlack * 1000;

    long slack = (runningLateSlack + runningEarlySlack) * 1000;

    for (Pair<FrequencyStopTripIndex> pair : indexPairs) {

      FrequencyStopTripIndex sourceStopIndex = findDepartures ? pair.getFirst()
          : pair.getSecond();
      FrequencyStopTripIndex destStopIndex = findDepartures ? pair.getSecond()
          : pair.getFirst();

      List<FrequencyBlockStopTimeEntry> sourceStopTimes = sourceStopIndex.getFrequencyStopTimes();
      List<FrequencyBlockStopTimeEntry> destStopTimes = destStopIndex.getFrequencyStopTimes();

      List<Date> serviceDates = _calendarService.getServiceDatesForInterval(
          sourceStopIndex.getServiceIds(),
          sourceStopIndex.getServiceInterval(), slackAdjustedTime,
          findDepartures);

      for (Date serviceDate : serviceDates) {

        ServiceInterval destServiceInterval = destStopIndex.getServiceInterval();

        if (serviceDateIsBeyondRangeOfQueue(nBestQueue, serviceDate,
            destServiceInterval, resultCount, findDepartures, slack)) {

          /**
           * The service date is beyond our worst departure-arrival, so we break
           */
          break;
        }

        int relativeTime = effectiveTime(serviceDate.getTime(),
            slackAdjustedTime);

        IndexAdapter<HasIndexedFrequencyBlockTrips> adapter = findDepartures
            ? IndexAdapters.FREQUENCY_END_TIME_INSTANCE
            : IndexAdapters.FREQUENCY_START_TIME_INSTANCE;

        int sourceIndex = GenericBinarySearch.search(sourceStopIndex,
            sourceStopIndex.size(), relativeTime, adapter);

        /**
         * When searching for arrival times, the index is an upper bound, so we
         * have to decrement to find the first good stop index
         */
        if (!findDepartures)
          sourceIndex--;

        if (0 <= sourceIndex && sourceIndex < sourceStopIndex.size()) {

          FrequencyBlockStopTimeEntry sourceEntry = sourceStopTimes.get(sourceIndex);
          BlockStopTimeEntry sourceBst = sourceEntry.getStopTime();
          FrequencyEntry frequency = sourceEntry.getFrequency();
          int stopTimeOffset = sourceEntry.getStopTimeOffset();

          int frequencyOffset = computeFrequencyOffset(relativeTime, sourceBst,
              frequency, stopTimeOffset, findDepartures);

          StopTimeInstance stiSource = new StopTimeInstance(sourceBst,
              serviceDate.getTime(), frequency, frequencyOffset);

          FrequencyBlockStopTimeEntry toEntry = destStopTimes.get(sourceIndex);
          BlockStopTimeEntry stopTimeTo = toEntry.getStopTime();
          StopTimeInstance stiDest = new StopTimeInstance(stopTimeTo,
              serviceDate.getTime(), frequency, frequencyOffset);

          /**
           * There's a chance the frequency-based departure+arrival pair could
           * extend out the front of the frequency range, at which point we
           * discard it.
           */
          if (!findDepartures
              && stiDest.getDepartureTime() < serviceDate.getTime()
                  + frequency.getStartTime() * 1000) {
            break;
          }

          if (stopTimeIsBeyondRangeOfQueue(nBestQueue, stiDest, resultCount,
              findDepartures, slack))
            break;

          Pair<StopTimeInstance> stiPair = findDepartures ? Tuples.pair(
              stiSource, stiDest) : Tuples.pair(stiDest, stiSource);

          /**
           * We only add to the n-best queue if the arrival-departure is beyond
           * the target time, as opposed to time adjusted by slack
           */
          if (isStopTimeInstanceBeyondTargetTime(stiSource, targetTime,
              findDepartures)) {
            nBestQueue.add(stiPair);
          }

          while (nBestQueue.size() > resultCount)
            nBestQueue.poll();

          /**
           * We always add to the result queue
           */
          resultQueue.add(stiPair);

          while (!resultQueue.isEmpty()) {
            Pair<StopTimeInstance> r = resultQueue.peek();
            StopTimeInstance sti = findDepartures ? r.getSecond()
                : r.getFirst();
            if (stopTimeIsBeyondRangeOfQueue(nBestQueue, sti, resultCount,
                findDepartures, slack))
              resultQueue.poll();
            else
              break;
          }
        }
      }
    }
  }

  private int computeFrequencyOffset(int relativeTime,
      BlockStopTimeEntry sourceBst, FrequencyEntry frequency,
      int stopTimeOffset, boolean findDepartures) {

    int t = Math.max(relativeTime, frequency.getStartTime());
    t = Math.min(t, frequency.getEndTime());
    t = snapToFrequencyStopTime(frequency, t, stopTimeOffset, findDepartures);
    return t - sourceBst.getStopTime().getDepartureTime();
  }

  private boolean serviceDateIsBeyondRangeOfQueue(
      PriorityQueue<Pair<StopTimeInstance>> queue, Date serviceDate,
      ServiceInterval interval, int resultCount, boolean findDepartures,
      long slack) {

    if (queue.size() != resultCount)
      return false;

    Pair<StopTimeInstance> stiPair = queue.peek();

    if (findDepartures) {
      /**
       * If we're looking for departures, then our queue is sorted by arrival
       * time at the toStop. Thus, if the latest arrival time in the queue is
       * less than the serviceDate, we return true.
       */
      return stiPair.getSecond().getArrivalTime() + slack < serviceDate.getTime()
          + interval.getMinArrival() * 1000;
    } else {
      /**
       * If we're looking for arrivals, then our queue is sorted by departure
       * time at the fromStop. Thus, if the earliest departure time in the queue
       * is more than the serviceDate, we return true.
       */
      return stiPair.getFirst().getDepartureTime() - slack > serviceDate.getTime()
          + interval.getMaxDeparture() * 1000;
    }
  }

  private boolean stopTimeIsBeyondRangeOfQueue(
      PriorityQueue<Pair<StopTimeInstance>> queue, StopTimeInstance sti,
      int resultCount, boolean findDepartures, long slack) {

    if (queue.size() != resultCount)
      return false;

    Pair<StopTimeInstance> stiPair = queue.peek();

    if (findDepartures) {
      /**
       * If we're looking for departures, then our queue is sorted by arrival
       * time at the toStop. Thus, if the latest arrival time in the queue is
       * less than the sti arrival time, we return true.
       */
      return stiPair.getSecond().getArrivalTime() + slack < sti.getArrivalTime();
    } else {
      /**
       * If we're looking for arrivals, then our queue is sorted by departure
       * time at the fromStop. Thus, if the earliest departure time in the queue
       * is more than the sti departure time, we return true.
       */
      return stiPair.getFirst().getDepartureTime() - slack > sti.getDepartureTime();
    }
  }

  private boolean isStopTimeInstanceBeyondTargetTime(
      StopTimeInstance stiSource, long targetTime, boolean findDepartures) {
    if (findDepartures)
      return targetTime <= stiSource.getDepartureTime();
    else
      return targetTime >= stiSource.getArrivalTime();
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

  private static class FirstDepartureTimeComparator implements
      Comparator<Pair<StopTimeInstance>> {

    @Override
    public int compare(Pair<StopTimeInstance> o1, Pair<StopTimeInstance> o2) {
      long t1 = o1.getFirst().getDepartureTime();
      long t2 = o2.getFirst().getDepartureTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }

  private static class LastArrivalTimeComparator implements
      Comparator<Pair<StopTimeInstance>> {

    @Override
    public int compare(Pair<StopTimeInstance> o1, Pair<StopTimeInstance> o2) {
      long t1 = o1.getSecond().getArrivalTime();
      long t2 = o2.getSecond().getArrivalTime();
      return t1 == t2 ? 0 : (t1 < t2 ? 1 : -1);
    }
  }
}
