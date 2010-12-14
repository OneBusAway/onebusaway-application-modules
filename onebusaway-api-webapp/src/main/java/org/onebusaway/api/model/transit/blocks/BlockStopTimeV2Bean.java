package org.onebusaway.api.model.transit.blocks;

import org.onebusaway.api.model.transit.schedule.StopTimeV2Bean;

public class BlockStopTimeV2Bean {

  private int blockSequence;

  private double distanceAlongBlock;

  private double accumulatedSlackTime;

  private StopTimeV2Bean stopTime;

  public int getBlockSequence() {
    return blockSequence;
  }

  public void setBlockSequence(int blockSequence) {
    this.blockSequence = blockSequence;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public double getAccumulatedSlackTime() {
    return accumulatedSlackTime;
  }

  public void setAccumulatedSlackTime(double accumulatedSlackTime) {
    this.accumulatedSlackTime = accumulatedSlackTime;
  }

  public StopTimeV2Bean getStopTime() {
    return stopTime;
  }

  public void setStopTime(StopTimeV2Bean stopTime) {
    this.stopTime = stopTime;
  }
}
