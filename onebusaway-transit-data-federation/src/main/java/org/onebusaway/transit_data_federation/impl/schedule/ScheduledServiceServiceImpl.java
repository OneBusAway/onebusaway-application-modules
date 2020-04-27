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
package org.onebusaway.transit_data_federation.impl.schedule;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ScheduleHelperService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.StopTimeService.EFrequencyStopTimeBehavior;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledServiceServiceImpl implements ScheduleHelperService {
  
  private static final long SCHEDULE_WINDOW_BEFORE = 15 * 60 * 1000;

  private static final long SCHEDULE_WINDOW_AFTER = 60 * 60 * 1000;

  @Autowired
  private BlockCalendarService _blockCalendarService;
  
  @Autowired
  private BlockIndexService _blockIndexService;

  @Autowired
  private StopTimeService _stopTimeService;

  @Autowired
  private TransitGraphDao _transitGraphDao;

  @Override
  public Boolean routeHasUpcomingScheduledService(String agencyId, long time, String routeId, String directionId) {    
    long serviceStart = time - SCHEDULE_WINDOW_BEFORE;
    long serviceEnd = time + SCHEDULE_WINDOW_AFTER;
    
    AgencyAndId routeAndId;
    if (routeId != null && routeId.contains("_")) 
      routeAndId = AgencyAndIdLibrary.convertFromString(routeId);
    else
      routeAndId = new AgencyAndId(agencyId, routeId);
    
    List<BlockInstance> instances = 
        _blockCalendarService.getActiveBlocksForRouteInTimeRange(routeAndId, serviceStart, serviceEnd);
    
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

        if(tripEntry.getRoute().getId().toString().equals(routeId)) {
          if (tripEntry.getDirectionId() == null || tripEntry.getDirectionId().equals(directionId)) {
            return true;
          }
        }
      }
    }
    
    return false;
  }

  @Override
  public Boolean stopHasUpcomingScheduledService(String stopAgencyId, long time, String stopId, String routeId, String directionId) {
    
    AgencyAndId stopAndId = null;
    if (stopId != null && stopId.contains("_"))
      stopAndId = AgencyAndIdLibrary.convertFromString(stopId);
    else if (stopId != null)
      stopAndId = new AgencyAndId(stopAgencyId, stopId);
    StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopAndId);
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

      if(tripEntry.getRoute().getId().toString().equals(routeId)) {
        if (tripEntry.getDirectionId() == null || tripEntry.getDirectionId().equals(directionId)) {
        return true;
        }
      }
    }

    return false;
  }

  @Override
  public Boolean stopHasRevenueServiceOnRoute(String agencyId, String stopId,
          String routeId, String directionId) {
      AgencyAndId stopAndId = null;
      if (stopId != null && stopId.contains("_"))
        stopAndId = AgencyAndIdLibrary.convertFromString(stopId);
      else if (stopId != null)
        stopAndId = new AgencyAndId(agencyId, stopId);
      
      StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopAndId);            
      List<BlockStopTimeIndex> stopTimeIndicesForStop = _blockIndexService.getStopTimeIndicesForStop(stopEntry);
      
      for (BlockStopTimeIndex bsti: stopTimeIndicesForStop) {
          List<BlockStopTimeEntry> stopTimes = bsti.getStopTimes();
          for (BlockStopTimeEntry bste: stopTimes) {
              StopTimeEntry stopTime = bste.getStopTime();
              
              TripEntry theTrip = stopTime.getTrip();
              
              if (routeId != null && !theTrip.getRoute().getId().toString().equals(routeId)) {
                  continue;
              }
              if (directionId !=null && theTrip.getDirectionId() != null) {
                if (!theTrip.getDirectionId().equals(directionId)) {
                    continue;
                }
              }
              
              /*
               * If at least one stoptime on one trip (subject to the
               * route and direction filters above) permits unrestricted
               * pick-up or drop-off at this stop (type=0), then it is
               * considered a 'revenue' stop.
               */
              if (stopTime.getDropOffType() == 0 ||
                      stopTime.getPickupType() == 0) {
                  return true;
              } 
          }
      }
      
      return false;
  }

  @Override
  public Boolean stopHasRevenueService(String agencyId, String stopId) {
      return this.stopHasRevenueServiceOnRoute(agencyId, stopId,
              null, null);
  }

  @Override
  public List<String> getSearchSuggestions(String agencyId, String input) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<StopBean> filterRevenueService(AgencyBean agency,
      StopsBean stops) {
    // TODO Auto-generated method stub
    return stops.getStops();
  }
  
}