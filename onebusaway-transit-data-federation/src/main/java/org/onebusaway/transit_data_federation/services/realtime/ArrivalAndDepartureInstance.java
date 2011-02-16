/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class ArrivalAndDepartureInstance {

  private static final long serialVersionUID = 3L;

  private final BlockInstance blockInstance;

  private final BlockStopTimeEntry blockStopTime;

  private long scheduledArrivalTime;

  private long scheduledDepartureTime;

  private BlockLocation blockLocation;

  private long predictedArrivalTime;

  private long predictedDepartureTime;

  public ArrivalAndDepartureInstance(BlockInstance blockInstance,
      BlockStopTimeEntry blockStopTime, ArrivalAndDepartureTime scheduledTime) {
    if (blockInstance == null)
      throw new IllegalArgumentException("blockInstance is null");
    if (blockStopTime == null)
      throw new IllegalArgumentException("blockStopTime is null");
    this.blockInstance = blockInstance;
    this.blockStopTime = blockStopTime;
    this.scheduledArrivalTime = scheduledTime.getArrivalTime();
    this.scheduledDepartureTime = scheduledTime.getDepartureTime();
  }

  public ArrivalAndDepartureInstance(BlockInstance blockInstance,
      BlockStopTimeEntry blockStopTime) {
    this(blockInstance, blockStopTime,
        ArrivalAndDepartureTime.getScheduledTime(blockInstance, blockStopTime));
  }

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public BlockStopTimeEntry getBlockStopTime() {
    return blockStopTime;
  }

  public BlockLocation getBlockLocation() {
    return blockLocation;
  }

  public void setBlockLocation(BlockLocation blockLocation) {
    this.blockLocation = blockLocation;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public boolean isPredictedArrivalTimeSet() {
    return predictedArrivalTime != 0;
  }

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public boolean isPredictedDepartureTimeSet() {
    return predictedDepartureTime != 0;
  }

  public long getPredictedDepartureTime() {
    return predictedDepartureTime;
  }

  public void setPredictedDepartureTime(long predictedDepartureTime) {
    this.predictedDepartureTime = predictedDepartureTime;
  }

  /****
   * Convenience Methods
   ****/

  public long getServiceDate() {
    return blockInstance.getServiceDate();
  }

  public long getBestArrivalTime() {
    if (isPredictedArrivalTimeSet())
      return getPredictedArrivalTime();
    return getScheduledArrivalTime();
  }

  public long getBestDepartureTime() {
    if (isPredictedDepartureTimeSet())
      return getPredictedDepartureTime();
    return getScheduledDepartureTime();
  }

  public FrequencyEntry getFrequency() {
    return blockInstance.getFrequency();
  }

  public BlockTripEntry getBlockTrip() {
    return blockStopTime.getTrip();
  }

  public StopEntry getStop() {
    return blockStopTime.getStopTime().getStop();
  }

  @Override
  public String toString() {
    return "ArrivalAndDepartureInstance(" + blockStopTime + ")";
  }
}
