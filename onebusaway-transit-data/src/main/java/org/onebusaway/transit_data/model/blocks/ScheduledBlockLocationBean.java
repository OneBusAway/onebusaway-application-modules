package org.onebusaway.transit_data.model.blocks;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class ScheduledBlockLocationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private BlockTripBean activeTrip;

  private int scheduledTime;

  private CoordinatePoint location;

  private double distanceAlongBlock = Double.NaN;

  private boolean inService;

  private int stopTimeIndex;

  public BlockTripBean getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(BlockTripBean activeTrip) {
    this.activeTrip = activeTrip;
  }

  public int getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(int scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  public int getStopTimeIndex() {
    return stopTimeIndex;
  }

  public void setStopTimeIndex(int stopTimeIndex) {
    this.stopTimeIndex = stopTimeIndex;
  }
}