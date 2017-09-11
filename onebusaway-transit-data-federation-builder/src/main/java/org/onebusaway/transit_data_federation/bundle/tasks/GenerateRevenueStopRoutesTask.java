/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenerateRevenueStopRoutesTask implements Runnable {
  
  private Logger _log = LoggerFactory.getLogger(GenerateRevenueStopRoutesTask.class);

  private FederatedTransitDataBundle _bundle;
  
  private GtfsRelationalDao _gtfsDao;
  
  private RefreshService _refreshService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }
  
  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }
  
  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }
  
  @Override
  public void run() {
    try {
      Map<AgencyAndId, HashSet<String>> revenueStopRouteIndex = new HashMap<AgencyAndId, HashSet<String>>();      
      Collection<StopTime> stopTimes = _gtfsDao.getAllStopTimes();
      for (StopTime stopTime : stopTimes) {
        if(stopTime.getDropOffType() == 0 || stopTime.getPickupType() == 0){
          Trip trip = stopTime.getTrip();
          AgencyAndId stopId = stopTime.getStop().getId();
          AgencyAndId routeId = trip.getRoute().getId();
          String directionId = trip.getDirectionId();
                    
          if(revenueStopRouteIndex.get(stopId) == null){
            HashSet<String> routeDirectionSet = new HashSet<String>();
            routeDirectionSet.add(getHash(routeId, directionId));
            revenueStopRouteIndex.put(stopId, routeDirectionSet);
          }
          else{
            revenueStopRouteIndex.get(stopId).add(getHash(routeId, directionId));
          }       
        }
      }
      File path = _bundle.getRevenueStopRouteIndicesPath();
      ObjectSerializationLibrary.writeObject(path, revenueStopRouteIndex);

      _refreshService.refresh(RefreshableResources.REVENUE_STOP_ROUTE_INDEX);
    } catch (IOException e) {
      throw new IllegalStateException(
          "error serializing service calendar data", e);
    }
  }
  
  public String getHash(final AgencyAndId routeId, final String directionId){
    return AgencyAndId.convertToString(routeId) + "_" + directionId;
  }
}
