package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.collections.Range;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.blocks.IndexAdapters;
import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
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

    StopEntry stopEntry = getStop(stopId);
    return getStopTimeInstancesInTimeRange(stopEntry, from, to);
  }

  @Override
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(
      StopEntry stopEntry, Date from, Date to) {

    List<StopTimeInstance> stopTimeInstances = new ArrayList<StopTimeInstance>();

    for (BlockStopTimeIndex index : _blockIndexService.getStopTimeIndicesForStop(stopEntry)) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getStopTimesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
          to, stopTimeInstances);
    }

    for (FrequencyBlockStopTimeIndex index : _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry)) {
      Collection<Date> serviceDates = _calendarService.getServiceDatesWithinRange(
          index.getServiceIds(), index.getServiceInterval(), from, to);
      getFrequenciesForStopAndServiceIdsAndTimeRange(index, serviceDates, from,
          to, stopTimeInstances);
    }

    return stopTimeInstances;
  }

  @Override
  public List<StopTimeInstance> getNextScheduledBlockTripDeparturesForStop(
      StopEntry stopEntry, long time) {

    List<StopTimeInstance> stopTimeInstances = new ArrayList<StopTimeInstance>();

    List<BlockStopTripIndex> blockStopTripIndices = _blockIndexService.getStopTripIndicesForStop(stopEntry);

    for (BlockStopTripIndex index : blockStopTripIndices) {

      List<Date> serviceDates = _calendarService.getNextServiceDatesForDepartureInterval(
          index.getServiceIds(), index.getServiceInterval(), time);

      for (Date serviceDate : serviceDates) {

        int relativeFrom = effectiveTime(serviceDate.getTime(), time);

        int fromIndex = GenericBinarySearch.search(index, index.size(),
            relativeFrom, IndexAdapters.BLOCK_STOP_TRIP_DEPARTURE_INSTANCE);

        if (fromIndex < index.size()) {
          BlockStopTimeEntry blockStopTime = index.getBlockStopTimeForIndex(fromIndex);
          StopTimeInstance sti = new StopTimeInstance(blockStopTime,
              serviceDate);
          stopTimeInstances.add(sti);
        }
      }
    }

    return stopTimeInstances;

  }

  @Override
  public Range getDepartureForStopAndServiceDate(AgencyAndId stopId,
      ServiceDate serviceDate) {

    StopEntry stop = getStop(stopId);

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
  public StopTimeInstance getNextStopTimeInstance(StopTimeInstance instance) {
    BlockStopTimeEntry bst = instance.getStopTime();
    if (!bst.hasNextStop())
      return null;
    bst = bst.getNextStop();
    StopTimeInstance sti = new StopTimeInstance(bst, instance.getServiceDate());
    return sti;
  }

  /****
   * Private Methods
   ****/

  private void getStopTimesForStopAndServiceIdsAndTimeRange(
      BlockStopTimeIndex index, Collection<Date> serviceDates, Date from,
      Date to, List<StopTimeInstance> stopTimeInstances) {

    List<BlockStopTimeEntry> blockStopTimes = index.getStopTimes();

    for (Date serviceDate : serviceDates) {

      int relativeFrom = effectiveTime(serviceDate, from);
      int relativeTo = effectiveTime(serviceDate, to);

      int fromIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
          relativeFrom, IndexAdapters.BLOCK_STOP_TIME_DEPARTURE_INSTANCE);
      int toIndex = GenericBinarySearch.search(index, blockStopTimes.size(),
          relativeTo, IndexAdapters.BLOCK_STOP_TIME_ARRIVAL_INSTANCE);

      for (int in = fromIndex; in < toIndex; in++) {
        BlockStopTimeEntry blockStopTime = blockStopTimes.get(in);
        stopTimeInstances.add(new StopTimeInstance(blockStopTime, serviceDate));
      }
    }

  }

  private void getFrequenciesForStopAndServiceIdsAndTimeRange(
      FrequencyBlockStopTimeIndex index, Collection<Date> serviceDates,
      Date from, Date to, List<StopTimeInstance> stopTimeInstances) {

    for (Date serviceDate : serviceDates) {

      int relativeFrom = effectiveTime(serviceDate, from);
      int relativeTo = effectiveTime(serviceDate, to);

      int fromIndex = GenericBinarySearch.search(index, index.size(),
          relativeFrom, IndexAdapters.FREQUENCY_END_TIME_INSTANCE);
      int toIndex = GenericBinarySearch.search(index, index.size(), relativeTo,
          IndexAdapters.FREQUENCY_START_TIME_INSTANCE);

      List<FrequencyBlockStopTimeEntry> frequencyStopTimes = index.getFrequencyStopTimes();
      for (int in = fromIndex; in < toIndex; in++) {
        FrequencyBlockStopTimeEntry entry = frequencyStopTimes.get(in);
        stopTimeInstances.add(new StopTimeInstance(entry.getStopTime(),
            serviceDate.getTime(), entry.getFrequency()));
      }
    }
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

  private StopEntry getStop(AgencyAndId stopId) {
    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    if (stopEntry == null)
      throw new NoSuchStopServiceException(
          AgencyAndIdLibrary.convertToString(stopId));
    return stopEntry;
  }

  private static final int effectiveTime(Date serviceDate, Date targetTime) {
    return effectiveTime(serviceDate.getTime(), targetTime.getTime());
  }

  private static final int effectiveTime(long serviceDate, long targetTime) {
    return (int) ((targetTime - serviceDate) / 1000);
  }

}
