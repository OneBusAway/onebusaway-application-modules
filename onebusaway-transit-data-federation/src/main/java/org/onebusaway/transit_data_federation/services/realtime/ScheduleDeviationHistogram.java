package org.onebusaway.transit_data_federation.services.realtime;

public class ScheduleDeviationHistogram {
  private final int[] scheduleDeviations;
  private final int[] counts;

  public ScheduleDeviationHistogram(int[] scheduleDeviations, int[] counts) {
    this.scheduleDeviations = scheduleDeviations;
    this.counts = counts;
  }

  public int[] getScheduleDeviations() {
    return scheduleDeviations;
  }

  public int[] getCounts() {
    return counts;
  }
}
