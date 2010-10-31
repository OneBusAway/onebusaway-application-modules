package org.onebusaway.transit_data_federation.services.blocks;

import java.io.Serializable;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;

/**
 * Specifies an immutable interval of min and max arrival and departure times.
 * 
 * @author bdferris
 * 
 */
public final class FrequencyServiceIntervalBlock implements Serializable,
    Comparable<FrequencyServiceIntervalBlock> {

  private static final long serialVersionUID = 1L;

  private final int[] startTimes;
  private final int[] endTimes;

  /**
   * 
   */
  public FrequencyServiceIntervalBlock(int[] startTimes, int[] endTimes) {
    this.startTimes = startTimes;
    this.endTimes = endTimes;

    if (startTimes.length != endTimes.length)
      throw new IllegalArgumentException("arrays must have same length");
  }

  /**
   * 
   * @return frequency block from times in seconds since midnight
   */
  public int[] getStartTimes() {
    return startTimes;
  }

  /**
   * 
   * @return frequency block to times in seconds since midnight
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
  public int compareTo(FrequencyServiceIntervalBlock o) {
    return startTimes[0] - o.startTimes[0];
  }
}
