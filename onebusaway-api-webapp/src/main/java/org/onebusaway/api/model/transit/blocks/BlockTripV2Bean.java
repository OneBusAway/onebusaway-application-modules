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
