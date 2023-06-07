/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 <inf71391@gmail.com>
 * Copyright (C) 2012 Google, Inc.
 * Copyright (C) 2015 University of South Florida
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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.*;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.DynamicBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.StaticBlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation for {@link StaticBlockLocationService}. Keeps a recent cache of
 * {@link BlockLocationRecord} records for current queries and can access
 * database persisted records for queries in the past.
 * 
 * @author bdferris
 * @see StaticBlockLocationService
 */
@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime:name=BlockLocationServiceImpl")
public class BlockLocationServiceImpl implements BlockLocationService, DynamicBlockLocationService {

  @Autowired
  @Qualifier("staticBlockLocationServiceImpl")
  private StaticBlockLocationService _staticBlockLocationService;
  @Autowired
  @Qualifier("dynamicBlockLocationServiceImpl")
  private DynamicBlockLocationService _dynamicBlockLocationService;

  public void setStaticBlockLocationService(StaticBlockLocationService staticBlockLocationService) {
    _staticBlockLocationService = staticBlockLocationService;
  }

  public void setDynamicBlockLocationService(DynamicBlockLocationService dynamicBlockLocationService) {
    _dynamicBlockLocationService = dynamicBlockLocationService;
  }

  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
      ((StaticBlockLocationServiceImpl)_staticBlockLocationService).handleVehicleLocationRecord(record);
  }

  public void handleVehicleLocationRecord(BlockInstance blockInstance, VehicleLocationRecord record) {
    _dynamicBlockLocationService.handleVehicleLocationRecord(blockInstance, record);
  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
    throw new UnsupportedOperationException();
  }

  public void resetVehicleLocation(AgencyAndId vehicleId) {
    // todo a little messy here
    ((StaticBlockLocationServiceImpl)_staticBlockLocationService).resetVehicleLocation(vehicleId);
  }

  @Override
  public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance, TargetTime time) {
    if (blockInstance.getBlock() instanceof DynamicBlockConfigurationEntryImpl) {
      return _dynamicBlockLocationService.getLocationForBlockInstance(blockInstance, time);
    }
    return _staticBlockLocationService.getLocationForBlockInstance(blockInstance, time);
  }

  @Override
  public Map<AgencyAndId, List<BlockLocation>> getLocationsForBlockInstance(BlockInstance blockInstance, List<Date> times, long currentTime) {
    return _staticBlockLocationService.getLocationsForBlockInstance(blockInstance, times, currentTime);
  }

  @Override
  public BlockLocation getLocationForBlockInstanceAndScheduledBlockLocation(BlockInstance blockInstance, ScheduledBlockLocation scheduledLocation, long targetTime) {
    return _staticBlockLocationService.getLocationForBlockInstanceAndScheduledBlockLocation(blockInstance, scheduledLocation, targetTime);
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(BlockInstance blockInstance, TargetTime time) {
    List<BlockLocation> list = new ArrayList<>();

     List<BlockLocation> staticLocations = _staticBlockLocationService.getLocationsForBlockInstance(blockInstance, time);
     if (staticLocations != null)
       list.addAll(staticLocations);
     if (_dynamicBlockLocationService != null && list.isEmpty()) { // if we have a static entry, don't look for a dynamic
       List<BlockLocation> dynamicLocations = _dynamicBlockLocationService.getLocationsForBlockInstance(blockInstance, time);
       if (dynamicLocations != null)
         list.addAll(dynamicLocations);
     }
     return list;
  }

  @Override
  public BlockLocation getScheduledLocationForBlockInstance(BlockInstance blockInstance, long targetTime) {
    // although this references the static, dynamic is implicitly included included
    return _staticBlockLocationService.getScheduledLocationForBlockInstance(blockInstance, targetTime);
  }

  @Override
  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId, TargetTime time) {
    return _staticBlockLocationService.getLocationForVehicleAndTime(vehicleId, time);
  }

  @Override
  public void register(BlockInstance blockInstance, VehicleLocationRecord record) {
    _dynamicBlockLocationService.register(blockInstance, record);
  }
}
