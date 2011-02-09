package org.onebusaway.transit_data_federation.model.tripplanner;

public class TripPlannerConstants {

  /**
   * That'd be 2.5 mph => 1.1176 meters/sec => 0.0011176 meters/ms
   */
  private static final double WALKING_SPEED_METERS_PER_MS = 0.0011176;

  /**
   * That'd be 68 mph => 30 meters/sec => 0.030 meters/ms
   */
  private static final double MAX_TRANSIT_VELOCITY = 0.03039872;

  /**
   * That'd be 1/2 mile => 804 meters
   */
  private static final int MAX_TRANSFER_DISTANCE = 804;

  /**
   * @return walking velocity in meters/ms
   */
  public double getWalkingVelocity() {
    return WALKING_SPEED_METERS_PER_MS;
  }

  /**
   * @return max transfier distance, in meters
   */
  public double getMaxTransferDistance() {
    return MAX_TRANSFER_DISTANCE; // About 1/2 mile
  }

  /**
   * @return max transit vehicle velocity, in meters per millisecond
   */
  public double getMaxTransitVelocity() {
    return MAX_TRANSIT_VELOCITY;
  }

  /**
   * 
   * @return min transfer time, in milliseconds
   */
  public long getMinTransferTime() {
    return 3 * 60 * 1000;
  }

  public double getInitialMaxDistanceToWalkNode() {
    return 60.96;
  }

  public double getMaxDistanceToWalkNode() {
    return 304.8;
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
