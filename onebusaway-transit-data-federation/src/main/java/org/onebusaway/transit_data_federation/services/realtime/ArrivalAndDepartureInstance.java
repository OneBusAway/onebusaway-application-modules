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
package org.onebusaway.transit_data_federation.services.realtime;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.model.TimeIntervalBean;
import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;

public class ArrivalAndDepartureInstance {

  private static final long serialVersionUID = 3L;

  private static DateFormat _format = DateFormat.getTimeInstance(DateFormat.SHORT);

  private final StopTimeInstance stopTimeInstance;

  private long scheduledArrivalTime;

  private long scheduledDepartureTime;

  private BlockLocation blockLocation;

  private BlockSequence blockSequence;

  private long predictedArrivalTime;

  private TimeIntervalBean predictedArrivalInterval;

  private long predictedDepartureTime;

  private OccupancyStatus historicalOccupancy;

  private OccupancyStatus predictedOccupancy;

  private TimeIntervalBean predictedDepartureInterval;

  public ArrivalAndDepartureInstance(StopTimeInstance stopTimeInstance,
      ArrivalAndDepartureTime scheduledTime) {
    if (stopTimeInstance == null)
      throw new IllegalArgumentException("stopTimeInstance is null");
    this.stopTimeInstance = stopTimeInstance;
    this.scheduledArrivalTime = scheduledTime.getArrivalTime();
    this.scheduledDepartureTime = scheduledTime.getDepartureTime();
    this.historicalOccupancy = stopTimeInstance.getStopTime().getStopTime().getHistoricalOccupancy();
  }

  public ArrivalAndDepartureInstance(StopTimeInstance stopTimeInstance) {
    this(stopTimeInstance,
        ArrivalAndDepartureTime.getScheduledTime(stopTimeInstance));
  }

  public StopTimeInstance getStopTimeInstance() {
    return stopTimeInstance;
  }

  public BlockInstance getBlockInstance() {
    return stopTimeInstance.getBlockInstance();
  }

  public BlockStopTimeEntry getBlockStopTime() {
    return stopTimeInstance.getStopTime();
  }

  public BlockLocation getBlockLocation() {
    return blockLocation;
  }

  public void setBlockLocation(BlockLocation blockLocation) {
    this.blockLocation = blockLocation;
  }

  public BlockSequence getBlockSequence() {
    return blockSequence;
  }

  public void setBlockSequence(BlockSequence blockSequence) {
    this.blockSequence = blockSequence;
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

  public TimeIntervalBean getPredictedArrivalInterval() {
    return predictedArrivalInterval;
  }

  public void setPredictedArrivalInterval(
      TimeIntervalBean predictedArrivalInterval) {
    this.predictedArrivalInterval = predictedArrivalInterval;
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

  public OccupancyStatus getHistoricalOccupancy() { return historicalOccupancy; }

  public void setHistoricalOccupancy(OccupancyStatus historicalOccupancy) { this.historicalOccupancy = historicalOccupancy; }

  public OccupancyStatus getPredictedOccupancy() { return predictedOccupancy; }

  public void setPredictedOccupancy(OccupancyStatus predictedOccupancy) { this.predictedOccupancy = predictedOccupancy; }

  public TimeIntervalBean getPredictedDepartureInterval() {
    return predictedDepartureInterval;
  }

  public void setPredictedDepartureInterval(
      TimeIntervalBean predictedDepartureInterval) {
    this.predictedDepartureInterval = predictedDepartureInterval;
  }

  /****
   * Convenience Methods
   ****/

  public long getServiceDate() {
    return stopTimeInstance.getServiceDate();
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
    return stopTimeInstance.getFrequency();
  }
  
  public FrequencyEntry getFrequencyLabel() {
    return stopTimeInstance.getFrequencyLabel();
  }

  public BlockTripEntry getBlockTrip() {
    return stopTimeInstance.getTrip();
  }

  public BlockTripInstance getBlockTripInstance() {
    return new BlockTripInstance(stopTimeInstance.getTrip(),
        stopTimeInstance.getState());
  }

  public StopEntry getStop() {
    return stopTimeInstance.getStop();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("ArrivalAndDepartureInstance(");
    b.append("stop=");
    b.append(getStop().getId());
    b.append(",arrival=");
    b.append(_format.format(new Date(getBestArrivalTime())));
    b.append(",departure=");
    b.append(_format.format(new Date(getBestDepartureTime())));
    b.append(",block="
        + getBlockTrip().getBlockConfiguration().getBlock().getId());
    b.append(")");
    return b.toString();
  }
}
