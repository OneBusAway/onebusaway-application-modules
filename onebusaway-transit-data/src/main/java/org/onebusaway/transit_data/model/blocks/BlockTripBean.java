package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.trips.TripBean;

public final class BlockTripBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private TripBean trip;

  private List<BlockStopTimeBean> blockStopTimes;

  private int accumulatedSlackTime;

  private double distanceAlongBlock;

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public List<BlockStopTimeBean> getBlockStopTimes() {
    return blockStopTimes;
  }

  public void setBlockStopTimes(List<BlockStopTimeBean> blockStopTimes) {
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
