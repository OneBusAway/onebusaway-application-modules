package org.onebusaway.transit_data_federation.model.tripplanner;

public class TripPlannerPreferences {

  /**
   * That'd be 2.5 mph => 1.1176 meters/sec => 0.0011176 meters/ms
   */
  private double walkingVelocity = 1.1176;

  /**
   * That'd be 1/2 mile => 804 meters
   */
  private static final int MAX_TRANSFER_DISTANCE = 804;

  /**
   * Default minimum amount of time required for a transfer
   */
  private int minTransferBufferTime = 3 * 60;

  /**
   * @return walking velocity in meters/ms
   */
  public double getWalkingVelocity() {
    return walkingVelocity;
  }

  /**
   * @return max transfier distance, in meters
   */
  public double getMaxTransferDistance() {
    return MAX_TRANSFER_DISTANCE; // About 1/2 mile
  }

  /**
   * This is the minimal amount of time that we want a rider to show up at stop
   * before boarding a bus as a safety buffer
   * 
   * @return min transfer buffer time, in seconds
   */
  public int getMinTransferBufferTime() {
    return minTransferBufferTime;
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
