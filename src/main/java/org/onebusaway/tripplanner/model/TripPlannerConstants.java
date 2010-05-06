package org.onebusaway.tripplanner.model;

public class TripPlannerConstants {

  // Miles per hour
  private static final double WALKING_SPEED_MPH = 2.5;

  /**
   * That'd be WALKING_SPEED_MPH * (feet per mile) * (hours per ms)
   */
  private static final double WALKING_SPEED_FEET_PER_MS = WALKING_SPEED_MPH * (5280) * (1 / (60 * 60 * 1000.0));

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

  /**
   * From 'Transit Capacity and Quality of Service Manual' - Part 3 - Exhibit
   * 3.9
   * 
   * http://onlinepubs.trb.org/Onlinepubs/tcrp/tcrp100/part%203.pdf
   * 
   * Table of passenger perceptions of time. Given that actual in-vehicle time
   * seems to occur in real-time (penalty ratio of 1.0), how do passengers
   * perceived walking, waiting for the first vehicle, and waiting for a
   * transfer. In addition, is there an additive penalty for making a transfer
   * of any kind.
   */

  /**
   * 
   */
  public double getWalkTimePenaltyRatio() {
    return 2.2;
  }

  /**
   * 
   * @return
   */
  public double getInitialWaitTimePenaltyRatio() {
    return 2.1;
  }

  /**
   * 
   * @return
   */
  public double getTransferWaitTimePenaltyRatio() {
    return 2.5;
  }

  /**
   * Additive penalty for making a transfer in travel sequence. See also
   * {@link #isTransferPenaltyAdditive()} for details on handling multiple
   * transfers.
   * 
   * @return time, in ms, to add to the perceived travel time in the presence of
   *         a transfer
   */
  public long getTransferPenalty() {
    return 14 * 60 * 1000;
  }

  /**
   * Should the transfer penalty ({@link #getTransferPenalty()}) be added once
   * for each transfer in a travel sequence (return true) or should it be added
   * just once if transfers are present in a travel sequence, regardless of the
   * total number of transfers (return false).
   * 
   * @return
   */
  public boolean isTransferPenaltyAdditive() {
    return true;
  }
}
