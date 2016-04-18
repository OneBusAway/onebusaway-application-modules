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

import java.util.List;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;

import org.onebusaway.transit_data_federation.services.ScheduleHelperService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Trivial implementation of the ScheduleHelperService.  That is, it does nothing.
 *
 */
@Component
public class TrivialSchedulerHelperService implements ScheduleHelperService {

        private TransitGraphDao _graph;
    
        private BlockIndexService _blockIndexService;
        
        @Autowired
        public void setTransitGraphDao(TransitGraphDao graph) {
                _graph = graph;
        }
        
        @Autowired
        public void setBlockIndexService(BlockIndexService blockIndexService) {
                _blockIndexService = blockIndexService;
        }
    
	@Override
	public Boolean routeHasUpcomingScheduledService(String agencyId, long time, String routeId,
			String directionId) {
		return null;
	}

	@Override
	public Boolean stopHasUpcomingScheduledService(String agencyId, long time, String stopId,
			String routeId, String directionId) {
		return null;
	}

	@Override
	public List<String> getSearchSuggestions(String agencyId, String input) {
		return null;
	}

        @Override
        public Boolean stopHasRevenueServiceOnRoute(String agencyId, String stopId,
                String routeId, String directionId) {
            AgencyAndId stop = AgencyAndIdLibrary.convertFromString(stopId);
            AgencyAndId route = AgencyAndIdLibrary.convertFromString(routeId);
            StopEntry stopEntry = _graph.getStopEntryForId(stop);
            
            List<BlockStopTimeIndex> stopTimeIndicesForStop = _blockIndexService.getStopTimeIndicesForStop(stopEntry);
            
            for (BlockStopTimeIndex bsti: stopTimeIndicesForStop) {
                List<BlockStopTimeEntry> stopTimes = bsti.getStopTimes();
                for (BlockStopTimeEntry bste: stopTimes) {
                    StopTimeEntry stopTime = bste.getStopTime();
                    
                    TripEntry theTrip = stopTime.getTrip();
                    
                    if (!(theTrip.getRoute().getId().equals(route) &&                    
                    theTrip.getDirectionId().equals(directionId))) {
                        continue;
                    }
                    
                    if (stopTime.getDropOffType() == 0 ||
                            stopTime.getPickupType() == 0) {
                        return true;
                    }
                }
            }
            
            return false;
        }
}
