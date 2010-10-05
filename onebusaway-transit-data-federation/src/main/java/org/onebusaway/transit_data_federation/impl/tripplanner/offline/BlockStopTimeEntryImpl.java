package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class BlockStopTimeEntryImpl implements BlockStopTimeEntry {

  private StopTimeEntry stopTime;

  private BlockTripEntry trip;

  private int blockSequence;

  public BlockStopTimeEntryImpl(StopTimeEntry stopTime, int blockSequence,
      BlockTripEntry trip) {

    if (stopTime == null)
      throw new IllegalArgumentException("stopTime is null");
    if (trip == null)
      throw new IllegalArgumentException("trip is null");

    this.stopTime = stopTime;
    this.trip = trip;
    this.blockSequence = blockSequence;
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
  public double getDistaceAlongBlock() {
    return trip.getDistanceAlongBlock() + stopTime.getShapeDistTraveled();
  }

  @Override
  public int getAccumulatedSlackTime() {
    return trip.getAccumulatedSlackTime() + stopTime.getAccumulatedSlackTime();
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