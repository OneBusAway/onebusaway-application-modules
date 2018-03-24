/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.collections.Range;
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
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;

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
          InstanceState state = new InstanceState(serviceDate.getTime());
          StopTimeInstance sti = new StopTimeInstance(blockStopTime, state);
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
          InstanceState state = new InstanceState(serviceDate.getTime(),
              frequency);
          int stopTimeOffset = entry.getStopTimeOffset();
          int frequencyOffset = computeFrequencyOffset(relativeFrom, bst,
              frequency, stopTimeOffset, true);
          StopTimeInstance sti = new StopTimeInstance(bst, state,
              frequencyOffset);
          stopTimeInstances.add(sti);
        }
      }

    }
    return stopTimeInstances;

  }

  /****
   * Private Methods
   * 
   * @param includePrivateService TODO
   ****/

  private int computeFrequencyOffset(int relativeTime,
      BlockStopTimeEntry sourceBst, FrequencyEntry frequency,
      int stopTimeOffset, boolean findDepartures) {

    int t = Math.max(relativeTime, frequency.getStartTime());
    t = Math.min(t, frequency.getEndTime());
    t = snapToFrequencyStopTime(frequency, t, stopTimeOffset, findDepartures);
    return t - sourceBst.getStopTime().getDepartureTime();
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

    InstanceState state = new InstanceState(serviceDate.getTime());
    for (int in = fromIndex; in < toIndex; in++) {
      BlockStopTimeEntry blockStopTime = blockStopTimes.get(in);
      instances.add(new StopTimeInstance(blockStopTime, state));
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

      InstanceState state = new InstanceState(serviceDate.getTime(), frequency);

      switch (frequencyBehavior) {

        case INCLUDE_UNSPECIFIED: {
          stopTimeInstances.add(new StopTimeInstance(bst, state));
          offsetsIntoIndex.add(in);
          break;
        }
        case INCLUDE_INTERPOLATED: {

          int stopTimeOffset = entry.getStopTimeOffset();

          int tFrom = Math.max(relativeFrom, frequency.getStartTime());
          int tTo = Math.min(relativeTo, frequency.getEndTime());

          tFrom = snapToFrequencyStopTime(frequency, tFrom, stopTimeOffset,
              true);
          tTo = snapToFrequencyStopTime(frequency, tTo, stopTimeOffset, false);

          for (int t = tFrom; t <= tTo; t += frequency.getHeadwaySecs()) {
            int frequencyOffset = t - bst.getStopTime().getDepartureTime();
            stopTimeInstances.add(new StopTimeInstance(bst, state,
                frequencyOffset));
            offsetsIntoIndex.add(in);
          }
          break;
        }
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

  private static int effectiveTime(Date serviceDate, Date targetTime) {
    return effectiveTime(serviceDate.getTime(), targetTime.getTime());
  }

  private static int effectiveTime(long serviceDate, long targetTime) {
    return (int) ((targetTime - serviceDate) / 1000);
  }
}
