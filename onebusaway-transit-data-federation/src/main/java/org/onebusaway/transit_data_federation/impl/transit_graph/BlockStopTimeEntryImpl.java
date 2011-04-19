package org.onebusaway.transit_data_federation.impl.transit_graph;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class BlockStopTimeEntryImpl implements BlockStopTimeEntry {

  private final StopTimeEntry stopTime;

  private final BlockTripEntry trip;

  private final int blockSequence;

  private final boolean hasNextStop;

  public BlockStopTimeEntryImpl(StopTimeEntry stopTime, int blockSequence,
      BlockTripEntry trip, boolean hasNextStop) {

    if (stopTime == null)
      throw new IllegalArgumentException("stopTime is null");
    if (trip == null)
      throw new IllegalArgumentException("trip is null");

    this.stopTime = stopTime;
    this.trip = trip;
    this.blockSequence = blockSequence;
    this.hasNextStop = hasNextStop;
  }

  @Override
  public StopTimeEntry getStopTime() {
    return stopTime;
  }

  @Override
  public BlockTripEntry getTrip() {
    return trip;
  }

  @Override
  public int getBlockSequence() {
    return blockSequence;
  }

  @Override
  public double getDistanceAlongBlock() {
    return trip.getDistanceAlongBlock() + stopTime.getShapeDistTraveled();
  }

  @Override
  public int getAccumulatedSlackTime() {
    return trip.getAccumulatedSlackTime() + stopTime.getAccumulatedSlackTime();
  }

  @Override
  public boolean hasPreviousStop() {
    return blockSequence > 0;
  }

  @Override
  public boolean hasNextStop() {
    return hasNextStop;
  }

  @Override
  public BlockStopTimeEntry getNextStop() {
    return trip.getBlockConfiguration().getStopTimes().get(blockSequence + 1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockSequence;
    result = prime * result + stopTime.hashCode();
    result = prime * result + trip.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockStopTimeEntryImpl other = (BlockStopTimeEntryImpl) obj;
    if (blockSequence != other.blockSequence)
      return false;
    if (!stopTime.equals(other.stopTime))
      return false;
    if (!trip.equals(other.trip))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BlockStopTime(stopTime=" + stopTime + " blockSeq=" + blockSequence
        + " trip=" + trip + ")";
  }
}