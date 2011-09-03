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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class BlockTripEntryImpl implements BlockTripEntry, Serializable {

  private static final long serialVersionUID = 2L;

  private BlockConfigurationEntry blockConfiguration;

  private TripEntry trip;

  private short sequence;

  private short accumulatedStopTimeIndex;

  private int accumulatedSlackTime;

  private double distanceAlongBlock;

  private BlockTripEntry previousTrip;

  private BlockTripEntry nextTrip;

  private AbstractBlockTripIndex pattern;

  public void setTrip(TripEntry trip) {
    this.trip = trip;
  }

  public void setBlockConfiguration(BlockConfigurationEntry blockConfiguration) {
    this.blockConfiguration = blockConfiguration;
  }

  public void setSequence(short sequence) {
    this.sequence = sequence;
  }

  public void setAccumulatedStopTimeIndex(short accumulatedStopTimeIndex) {
    this.accumulatedStopTimeIndex = accumulatedStopTimeIndex;
  }

  public void setAccumulatedSlackTime(int accumulatedSlackTime) {
    this.accumulatedSlackTime = accumulatedSlackTime;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public void setPreviousTrip(BlockTripEntry previousTrip) {
    this.previousTrip = previousTrip;
  }

  public void setNextTrip(BlockTripEntry nextTrip) {
    this.nextTrip = nextTrip;
  }

  public void setPattern(AbstractBlockTripIndex pattern) {
    this.pattern = pattern;
  }

  /****
   * {@link BlockTripEntry} Interface
   ****/

  @Override
  public BlockConfigurationEntry getBlockConfiguration() {
    return blockConfiguration;
  }

  @Override
  public TripEntry getTrip() {
    return trip;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    List<BlockStopTimeEntry> stopTimes = blockConfiguration.getStopTimes();
    int toIndex = stopTimes.size();
    if (nextTrip != null)
      toIndex = nextTrip.getAccumulatedStopTimeIndex();
    return stopTimes.subList(accumulatedStopTimeIndex, toIndex);
  }

  @Override
  public short getSequence() {
    return sequence;
  }

  @Override
  public short getAccumulatedStopTimeIndex() {
    return accumulatedStopTimeIndex;
  }

  @Override
  public int getAccumulatedSlackTime() {
    return accumulatedSlackTime;
  }

  @Override
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  @Override
  public BlockTripEntry getPreviousTrip() {
    return previousTrip;
  }

  @Override
  public BlockTripEntry getNextTrip() {
    return nextTrip;
  }

  @Override
  public int getArrivalTimeForIndex(int stopIndex) {
    return trip.getStopTimes().get(stopIndex).getArrivalTime();
  }

  @Override
  public int getDepartureTimeForIndex(int stopIndex) {
    return trip.getStopTimes().get(stopIndex).getDepartureTime();
  }

  @Override
  public double getDistanceAlongBlockForIndex(int stopIndex) {
    return distanceAlongBlock
        + trip.getStopTimes().get(stopIndex).getShapeDistTraveled();
  }

  @Override
  public AbstractBlockTripIndex getPattern() {
    return pattern;
  }

  @Override
  public String toString() {
    return trip.getId().toString();
  }
}
