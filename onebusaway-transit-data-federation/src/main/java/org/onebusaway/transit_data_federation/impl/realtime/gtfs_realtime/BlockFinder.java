/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Methods for searching for a block across service days.
 *
 * WMATA presents some scenarios where blocks are not unique to
 * a service day causing some special case determinations to be necessary.
 */
public class BlockFinder {

  private static Logger _log = LoggerFactory.getLogger(BlockFinder.class);

  private final BlockCalendarService _blockCalendarService;
  public final Map<AgencyAndId, BlockServiceDate> _cache = new PassiveExpiringMap<>(30 * 60 * 1000);

  public BlockFinder(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void reset() {
    _cache.clear();
  }

  /**
   * We need a concept of service date -- the day the block is anchored in.
   * Because GTFS supports both negative start times and 25+ hour blocks,
   * this is not a simple lookup.
   */
  public BlockServiceDate getBlockServiceDateFromTrip(TripEntry tripEntry,
                                                      long currentTime) {
    if (!_cache.containsKey(tripEntry.getId())) { // note cache may contain null result
      BlockServiceDate blockServiceDate = getBlockServiceDateFromTripUnCached(tripEntry, currentTime);
      _cache.put(tripEntry.getId(), blockServiceDate);
    }
    return _cache.get(tripEntry.getId());
  }

  private BlockServiceDate getBlockServiceDateFromTripUnCached(TripEntry tripEntry,
                                                      long currentTime) {
    ServiceDate serviceDate;
    for (ServiceDate serviceDateGuess : getPossibleServiceDates(currentTime)) {
      BlockInstance blockInstance = _blockCalendarService.getBlockInstance(tripEntry.getBlock().getId(),
                serviceDateGuess.getAsDate().getTime());
      if (blockInstance != null) {
        serviceDate = new ServiceDate(new Date(blockInstance.getServiceDate()));
        Integer tripStartTime = getTripStartTime(blockInstance, tripEntry);
        if (tripStartTime != null) {
          int adjustedTripStartTime = getBlockStartTimeForTripStartTime(blockInstance, tripEntry.getId(), tripStartTime);
          if (adjustedTripStartTime > 0) {
            return new BlockServiceDate(serviceDate, blockInstance, adjustedTripStartTime);
          }
        }
      }
    }
    return null;
  }

  private Integer getTripStartTime(BlockInstance blockInstance, TripEntry tripEntry) {
    for (BlockTripEntry testTrip : blockInstance.getBlock().getTrips()) {
      if (testTrip.getTrip().getId().equals(tripEntry.getId()))
        return testTrip.getTrip().getStopTimes().get(0).getDepartureTime();
    }
    return null;
  }

  private List<ServiceDate> getPossibleServiceDates(long currentTime) {
    List<ServiceDate> possibleDates = new ArrayList<>();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(currentTime);
    // if we are less than 4:00 we could be previous start date
    if (cal.get(Calendar.HOUR_OF_DAY) < 4) {
      possibleDates.add(yesterday(currentTime));
    }
    // always check current date
    possibleDates.add(today(currentTime));
    // if we are past 20:00 we could be a next start time
    if (cal.get(Calendar.HOUR_OF_DAY) > 20) {
      possibleDates.add(tomorrow(currentTime));
    }
    return possibleDates;
  }

  private ServiceDate tomorrow(long currentTime) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(currentTime);
    cal.add(Calendar.DAY_OF_MONTH, +1);
    return new ServiceDate(cal);
  }

  private ServiceDate yesterday(long currentTime) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(currentTime);
    cal.add(Calendar.DAY_OF_MONTH, -1);
    return new ServiceDate(cal);
  }

  private ServiceDate today(long currentTime) {
    return new ServiceDate(new Date(currentTime));
  }

  /**
   * Calculate block start time from real-time trip start time value.
   * Frequency based trips are differentiated based on start time.
   * Scheduled based trips don't currently use this.
   */
  private int getBlockStartTimeForTripStartTime(BlockInstance instance,
                                                AgencyAndId tripId, int tripStartTime) {
    BlockConfigurationEntry block = instance.getBlock();
    if (block.getTrips() == null || block.getTrips().isEmpty()) {
      _log.debug("no trips for trip start time on block {}", block.getBlock().getId());
      return -1;
    }
    Map<AgencyAndId, BlockTripEntry> blockTripsById = null;
    try {
      blockTripsById = MappingLibrary.mapToValue(
              block.getTrips(), "trip.id");
    } catch (IllegalStateException ise) {
      if (block instanceof DynamicBlockConfigurationEntryImpl) {
        return block.getDepartureTimeForIndex(0); // no adjustment for now
      }
      _log.debug("invalid block {}", block.getBlock().getId());
      return -1;
    }
    int rawBlockStartTime = block.getDepartureTimeForIndex(0);

    if (!blockTripsById.containsKey(tripId)) {
      _log.debug("getBlockStartTimeForTripStartTime(" + instance + ", " + tripId + ", "
              + tripStartTime + ") did not find matching trip; aborting");
      return -1;
    }

    int rawTripStartTime = blockTripsById.get(tripId).getDepartureTimeForIndex(
            0);

    // here we adjust our block start time by the difference between the
    // real-time tripStartTime and our scheduled tripStartTime
    // if the result is negative our tripStartTime is likely invalid
    // recover gracefully by using the rawBlockStarTime
    int adjustedBlockStartTime = rawBlockStartTime
            + (tripStartTime - rawTripStartTime);

    if (adjustedBlockStartTime < 0) {
      return rawBlockStartTime;
    }
    return adjustedBlockStartTime;
  }

}
