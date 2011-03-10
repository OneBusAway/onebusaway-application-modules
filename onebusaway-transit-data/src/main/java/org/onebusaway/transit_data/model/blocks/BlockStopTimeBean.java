package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;

import org.onebusaway.transit_data.model.schedule.StopTimeBean;

public class BlockStopTimeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int blockSequence;

  private double distanceAlongBlock;

  private double accumulatedSlackTime;

  private StopTimeBean stopTime;

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

  public StopTimeBean getStopTime() {
    return stopTime;
  }

  public void setStopTime(StopTimeBean stopTime) {
    this.stopTime = stopTime;
  }
}
