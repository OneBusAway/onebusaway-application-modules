package org.onebusaway.api.model.transit.blocks;

public class BlockTripV2Bean {

  private String tripId;

  private int accumulatedSlackTime;

  private double distanceAlongBlock;

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
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
