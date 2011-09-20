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
package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

public class VehicleLocationCacheEntry {

  private final BlockInstance _blockInstance;

  private VehicleLocationCacheElements _elements;

  private boolean _closed = false;

  public VehicleLocationCacheEntry(BlockInstance blockInstance) {
    _blockInstance = blockInstance;
    _elements = new VehicleLocationCacheElements(blockInstance);
  }

  public BlockInstance getBlockInstance() {
    return _blockInstance;
  }

  public synchronized boolean isClosed() {
    return _closed;
  }

  /**
   * 
   * @param record
   * @param scheduledBlockLocation
   * @param samples
   * @return true if the element was successfully added, or false if the entry
   *         is closed to new elements
   */
  public synchronized boolean addElement(VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation,
      ScheduleDeviationSamples samples) {

    if (_closed)
      return false;

    VehicleLocationCacheElement element = new VehicleLocationCacheElement(
        record, scheduledBlockLocation, samples);

    _elements = _elements.extend(element);

    return true;
  }

  /**
   * 
   * @param blockInstance
   * @return true if closed
   */
  public synchronized boolean isClosedBecauseBlockInstanceChanged(
      BlockInstance blockInstance) {

    if (_closed)
      return true;

    if (!_blockInstance.equals(blockInstance)) {
      _closed = true;
      return true;
    }

    return false;
  }

  /**
   * 
   * @param time
   * @return true if closed
   */
  public synchronized boolean closeIfStale(long time) {

    if( _closed )
      return true;
    
    _elements = _elements.pruneOlderThanTime(time);

    if (_elements.isEmpty()) {
      _closed = true;
      return true;
    }

    return false;
  }

  public synchronized VehicleLocationCacheElements getElements() {
    return _elements;
  }

}