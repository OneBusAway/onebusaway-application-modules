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
package org.onebusaway.api.model.transit.blocks;

import java.util.List;

public class BlockTripV2Bean {

  private String tripId;

  private List<BlockStopTimeV2Bean> blockStopTimes;

  private int accumulatedSlackTime;

  private double distanceAlongBlock;

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public List<BlockStopTimeV2Bean> getBlockStopTimes() {
    return blockStopTimes;
  }

  public void setBlockStopTimes(List<BlockStopTimeV2Bean> blockStopTimes) {
    this.blockStopTimes = blockStopTimes;
  }

  public int getAccumulatedSlackTime() {
    return accumulatedSlackTime;
  }

  public void setAccumulatedSlackTime(int accumulatedSlackTime) {
    this.accumulatedSlackTime = accumulatedSlackTime;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }
}
