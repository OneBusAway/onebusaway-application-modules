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
package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class ScheduledBlockLocationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private BlockTripBean activeTrip;

  private int scheduledTime;

  private CoordinatePoint location;

  private double distanceAlongBlock = Double.NaN;

  private boolean inService;

  private int stopTimeIndex;

  public BlockTripBean getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(BlockTripBean activeTrip) {
    this.activeTrip = activeTrip;
  }

  public int getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(int scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  public int getStopTimeIndex() {
    return stopTimeIndex;
  }

  public void setStopTimeIndex(int stopTimeIndex) {
    this.stopTimeIndex = stopTimeIndex;
  }
}