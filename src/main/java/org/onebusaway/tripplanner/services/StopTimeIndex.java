package org.onebusaway.tripplanner.services;

public interface StopTimeIndex {

  /**
   * Find the closest previous {@link StopTimeInstanceProxy} (possibly more than
   * one) whose arrival time is less than or equal to the specified target time.
   * 
   * @param context
   * @param targetTime
   * @param hint
   * @return
   */
  public StopTimeIndexResult getPreviousStopTimeArrival(StopTimeIndexContext context, long targetTime, Object hint);

  /**
   * Find the next closest {@link StopTimeInstanceProxy} (possibly more than
   * one) whose departure time is greater than or equal to the specified target
   * time.
   * 
   * @param context
   * @param targetTime
   * @param hint
   * @return
   */
  public StopTimeIndexResult getNextStopTimeDeparture(StopTimeIndexContext context, long targetTime, Object hint);
}
