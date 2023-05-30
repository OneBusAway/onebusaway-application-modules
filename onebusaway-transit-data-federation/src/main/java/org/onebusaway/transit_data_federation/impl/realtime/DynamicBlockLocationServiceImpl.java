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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.DynamicBlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.DynamicBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElement;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElements;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.onebusaway.transit_data_federation.impl.realtime.DynamicBlockIndexServiceImpl.CACHE_TIMEOUT;

@Component
/**
 * Counterpart to (Static)BlockLocationService, handling dynamic trips
 * ( ADDED / DUPLICATED )
 * to refactor into a common location service.
 */
public class DynamicBlockLocationServiceImpl extends AbstractBlockLocationServiceImpl implements DynamicBlockLocationService {

  private static Logger _log = LoggerFactory.getLogger(DynamicBlockLocationServiceImpl.class);

  private Map<AgencyAndId, BlockLocation> _blockIdToBlockLocation = new PassiveExpiringMap<>(CACHE_TIMEOUT);
  private Map<AgencyAndId, RecordAndLocation> _vehicleIdToRecordAndLocation = new PassiveExpiringMap<>(CACHE_TIMEOUT);

  @Autowired
  @Qualifier("dynamicBlockIndexServiceImpl")
  private DynamicBlockIndexService _blockIndexService;
  @Autowired
  private ShapePointService _shapePointService;

  @Override
  public void register(BlockInstance blockInstance, VehicleLocationRecord record) {
    long targetTime = record.getTimeOfRecord();
    AgencyAndId blockId = blockInstance.getBlock().getBlock().getId();
    ScheduledBlockLocation scheduledBlockLocation = getScheduledBlockLocationForBlockInstance(blockInstance, targetTime);
    BlockLocation blockLocation = getBlockLocation(blockInstance, record, scheduledBlockLocation, targetTime);
    if (blockLocation != null) {
      this._blockIdToBlockLocation.put(blockId, blockLocation);
    }
  }

  @Override
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance, TargetTime time) {
    // this is a 1-1, much simpler than the static configuration
    return _blockIdToBlockLocation.get(blockInstance.getBlock().getBlock().getId());
  }

  @Override
  public List<BlockLocation> getLocationsForBlockInstance(BlockInstance blockInstance, TargetTime time) {
    List<BlockLocation> list = new ArrayList<>();
    BlockLocation locationForBlockInstance = getLocationForBlockInstance(blockInstance, time);
    if (locationForBlockInstance != null)
      list.add(locationForBlockInstance);
    return list;
  }

  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    // for performance reasons we require the block and record
    // for dynamic trips
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void handleVehicleLocationRecord(BlockInstance blockInstance, VehicleLocationRecord record) {

    _blockIndexService.register(blockInstance);
    register(blockInstance, record);

    ScheduledBlockLocation scheduledBlockLocation = getScheduledBlockLocationForVehicleLocationRecord(
            record, blockInstance);

    putBlockLocationRecord(blockInstance, record, scheduledBlockLocation);

  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp) {
    throw new UnsupportedOperationException();
  }

  private void putBlockLocationRecord(BlockInstance blockInstance, VehicleLocationRecord record, ScheduledBlockLocation scheduledBlockLocation) {
    if (record.getVehicleId() != null) {
      _vehicleIdToRecordAndLocation.put(record.getVehicleId(), new RecordAndLocation(record, scheduledBlockLocation));
    }
  }

  private BlockLocation getBlockLocation(BlockInstance blockInstance, VehicleLocationRecord record, ScheduledBlockLocation scheduledLocation, long targetTime) {
    VehicleLocationCacheElement element = new VehicleLocationCacheElement(record, scheduledLocation, null);
    VehicleLocationCacheElements elements = new VehicleLocationCacheElements(blockInstance, element);
    return getBlockLocation(blockInstance, elements, scheduledLocation, targetTime);
  }

  private static class RecordAndLocation {
    private VehicleLocationRecord record;
    private ScheduledBlockLocation location;
    public RecordAndLocation(VehicleLocationRecord record, ScheduledBlockLocation location) {
      this.record = record;
      this.location = location;
    }
  }
}
