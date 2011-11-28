/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

class TransitLegBuilder {

  private BlockTripInstance blockTripInstanceFrom = null;
  
  private BlockTripInstance blockTripInstanceTo = null;

  private long scheduledDepartureTime;
  private long scheduledArrivalTime;

  private long predictedDepartureTime;
  private long predictedArrivalTime;

  private ArrivalAndDepartureInstance fromStop;
  private ArrivalAndDepartureInstance toStop;
  
  private BlockTripEntry nextTrip = null;

  public BlockTripInstance getBlockTripInstanceFrom() {
    return blockTripInstanceFrom;
  }

  public void setBlockTripInstanceFrom(BlockTripInstance blockTripInstanceFrom) {
    this.blockTripInstanceFrom = blockTripInstanceFrom;
  }

  public BlockTripInstance getBlockTripInstanceTo() {
    return blockTripInstanceTo;
  }

  public void setBlockTripInstanceTo(BlockTripInstance blockTripInstanceTo) {
    this.blockTripInstanceTo = blockTripInstanceTo;
  }

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public long getPredictedDepartureTime() {
    return predictedDepartureTime;
  }

  public void setPredictedDepartureTime(long predictedDepartureTime) {
    this.predictedDepartureTime = predictedDepartureTime;
  }

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public ArrivalAndDepartureInstance getFromStop() {
    return fromStop;
  }

  public void setFromStop(ArrivalAndDepartureInstance fromStop) {
    this.fromStop = fromStop;
  }

  public ArrivalAndDepartureInstance getToStop() {
    return toStop;
  }

  public void setToStop(ArrivalAndDepartureInstance toStop) {
    this.toStop = toStop;
  }

  public BlockTripEntry getNextTrip() {
    return nextTrip;
  }

  public void setNextTrip(BlockTripEntry nextTrip) {
    this.nextTrip = nextTrip;
  }

  public long getBestDepartureTime() {
    if (predictedDepartureTime != 0)
      return predictedDepartureTime;
    return scheduledDepartureTime;
  }

  public long getBestArrivalTime() {
    if (predictedArrivalTime != 0)
      return predictedArrivalTime;
    return scheduledArrivalTime;
  }
}