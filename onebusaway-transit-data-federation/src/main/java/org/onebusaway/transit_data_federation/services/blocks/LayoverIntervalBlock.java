package org.onebusaway.transit_data_federation.services.blocks;

import java.io.Serializable;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;

/**
 * Specifies an immutable interval of min and max arrival and departure times.
 * 
 * @author bdferris
 * 
 */
public final class LayoverIntervalBlock implements Serializable,
    Comparable<LayoverIntervalBlock> {

  private static final long serialVersionUID = 1L;

  private final int[] startTimes;
  private final int[] endTimes;

  /**
   * 
   * @param startTimes start times in seconds since midnight
   * @param endTimes end times in seconds since midnight
   */
  public LayoverIntervalBlock(int[] startTimes, int[] endTimes) {
    this.startTimes = startTimes;
    this.endTimes = endTimes;

    if (startTimes.length != endTimes.length)
      throw new IllegalArgumentException("arrays must have same length");
  }

  /**
   * 
   * @return start times in seconds since midnight
   */
  public int[] getStartTimes() {
    return startTimes;
  }

  /**
   * 
   * @return end times in seconds since midnight
   */
  public int[] getEndTimes() {
    return endTimes;
  }

  public ServiceInterval getRange() {
    int n = startTimes.length - 1;
    return new ServiceInterval(startTimes[0], startTimes[0], endTimes[n],
        endTimes[n]);
  }

  @Override
  public int compareTo(LayoverIntervalBlock o) {
    return startTimes[0] - o.startTimes[0];
  }
}
