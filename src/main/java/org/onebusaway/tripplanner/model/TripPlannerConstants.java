package org.onebusaway.tripplanner.model;

public class TripPlannerConstants {

  // Miles per hour
  private static final double WALKING_SPEED_MPH = 2.5;

  private static final double WALKING_SPEED_FEET_PER_MS = WALKING_SPEED_MPH
      * 5280 /* feet/mile */* (1 / (60 * 60 * 1000.0)) /* hour/ms */;

  // That'd be 68 mph => 100 fps => 0.1 ft / ms
  private static final double MAX_TRANSIT_VELOCITY = 0.1;

  /**
   * 
   * @return walking velocity in feet/ms
   */
  public double getWalkingVelocity() {
    return WALKING_SPEED_FEET_PER_MS;
  }

  public double getMaxTransferDistance() {
    return 5280 / 2;
  }

  public double getMaxTransitVelocity() {
    return MAX_TRANSIT_VELOCITY;
  }

  public long getMinTransferTime() {
    return 3 * 60 * 1000;
  }

  public double getInitialMaxDistanceToWalkNode() {
    return 200;
  }

  public double getMaxDistanceToWalkNode() {
    return 1000;
  }
}
