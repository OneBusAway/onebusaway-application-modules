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

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
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

  private BlockCalendarService _blockCalendarService;
  public BlockFinder(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  /**
   * We need a concept of service date -- the day the block is anchored in.
   * Because GTFS supports both negative start times and 25+ hour blocks,
   * this is not a simple lookup.
   */
  public BlockServiceDate getBlockServiceDateFromTrip(TripEntry tripEntry,
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

  /*
        int tripStartTime = 0;
      int blockStartTime = 0;
      if (trip.hasStartTime() && !"0".equals(trip.getStartTime())) {
        try {
          Matcher m = _pattern.matcher(trip.getStartTime());
          if (!m.matches()) {
            long timeInMil = serviceDate.getAsDate().getTime();
            long epochTime = Long.parseLong(trip.getStartTime());
            long startTime = (epochTime - timeInMil) / 1000;
            tripStartTime = (int) startTime;
          } else
            tripStartTime = StopTimeFieldMappingFactory.getStringAsSeconds(trip.getStartTime());
        } catch (InvalidStopTimeException iste) {
          _log.debug("invalid stopTime of " + trip.getStartTime() + " for trip " + trip);
          return null;
        }
        blockStartTime = getBlockStartTimeForTripStartTime(instance,
                tripEntry.getId(), tripStartTime);
        if (blockStartTime < 0) {
          _log.debug("invalid blockStartTime for trip " + trip + " for instance=" + instance);
          return null;
        }
        blockDescriptor.setStartTime(blockStartTime);
      }

   */

  /*

    if (serviceDate != null) {
    	instance = _blockCalendarService.getBlockInstance(block.getId(),
    			serviceDate.getAsDate().getTime());
    	if (instance == null) {
    		_log.debug("block " + block.getId() + " does not exist on service date "
    				+ serviceDate);
    		return null;
    	}
    } else {
      // we have legacy support for missing service date
      // mostly for unit tests but also legacy feeds
    	long timeFrom = currentTime - 30 * 60 * 1000;
    	long timeTo = currentTime + 30 * 60 * 1000;

    	List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
    			block.getId(), timeFrom, timeTo);

    	if (instances.isEmpty()) {
    		instances = _blockCalendarService.getClosestActiveBlocks(block.getId(),
    				currentTime);
    	}

    	if (instances.isEmpty()) {
    		_log.debug("could not find any active instances for the specified block="
    				+ block.getId() + " trip=" + trip);
    		return null;
    	}
    	instance = instances.get(0);
    }

    if (serviceDate == null) {
    	serviceDate = new ServiceDate(new Date(instance.getServiceDate()));
    }

   */
}
