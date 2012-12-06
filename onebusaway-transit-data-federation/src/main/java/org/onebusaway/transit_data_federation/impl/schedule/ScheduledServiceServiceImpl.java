/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.impl.schedule;

import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.StopTimeService.EFrequencyStopTimeBehavior;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.schedule.ScheduledServiceService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledServiceServiceImpl implements ScheduledServiceService {
  
  private static final long SCHEDULE_WINDOW_BEFORE = 15 * 60 * 1000;

  private static final long SCHEDULE_WINDOW_AFTER = 60 * 60 * 1000;

  @Autowired
  private BlockCalendarService _blockCalendarService;

  @Autowired
  private StopTimeService _stopTimeService;

  @Autowired
  private TransitGraphDao _transitGraphDao;

  @Override
  public Boolean routeHasUpcomingScheduledService(long time, String _routeId, String directionId) {    
    long serviceStart = time - SCHEDULE_WINDOW_BEFORE;
    long serviceEnd = time + SCHEDULE_WINDOW_AFTER;

    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(_routeId);
    
    List<BlockInstance> instances = 
        _blockCalendarService.getActiveBlocksForRouteInTimeRange(routeId, serviceStart, serviceEnd);
    
    if(instances.isEmpty()) {
      return false;
    }

    for(BlockInstance instance : instances) {
      List<BlockTripEntry> tripsInBlock = instance.getBlock().getTrips();
      if(tripsInBlock.isEmpty()) {
        continue;
      }
      
      for(BlockTripEntry blockTripEntry : tripsInBlock) {
        TripEntry tripEntry = blockTripEntry.getTrip();

        if(tripEntry.getRoute().getId().equals(routeId) && tripEntry.getDirectionId().equals(directionId)) {
          return true;
        }
      }
    }
    
    return false;
  }

  @Override
  public Boolean stopHasUpcomingScheduledService(long time, String _stopId, String _routeId, String directionId) {
	AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(_routeId);
	AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(_stopId);
	  
	StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopId);
    if (stopEntry == null) {
      return null;
    }
    
    Date serviceStart = new Date(time - SCHEDULE_WINDOW_BEFORE);
    Date serviceEnd = new Date(time + SCHEDULE_WINDOW_AFTER);

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        stopEntry, serviceStart, serviceEnd,
        EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED);

    if(stis.isEmpty()) {
      return false;
    }
    
    for(StopTimeInstance stopTime : stis) {
      BlockTripEntry blockTripEntry = stopTime.getTrip();
      TripEntry tripEntry = blockTripEntry.getTrip();

      if(tripEntry.getRoute().getId().equals(routeId) && tripEntry.getDirectionId().equals(directionId)) {
        return true;
      }
    }

    return false;
  }
}
