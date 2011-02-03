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
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class ArrivalAndDepartureInstance {

  private static final long serialVersionUID = 3L;

  private final BlockInstance blockInstance;

  private final BlockStopTimeEntry blockStopTime;

  private BlockLocation blockLocation;

  private long predictedArrivalTime;

  private long predictedDepartureTime;

  public ArrivalAndDepartureInstance(BlockInstance blockInstance,
      BlockStopTimeEntry blockStopTime) {
    this.blockInstance = blockInstance;
    this.blockStopTime = blockStopTime;
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

  public long getScheduledArrivalTime() {
    return blockInstance.getServiceDate()
        + blockStopTime.getStopTime().getArrivalTime() * 1000;
  }

  public long getScheduledDepartureTime() {
    return blockInstance.getServiceDate()
        + blockStopTime.getStopTime().getDepartureTime() * 1000;
  }
  
  public FrequencyEntry getFrequency() {
    return blockInstance.getFrequency();
  }

  @Override
  public String toString() {
    return "ArrivalAndDepartureInstance(block=" + blockInstance + ")";
  }

}
