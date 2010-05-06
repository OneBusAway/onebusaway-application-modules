package org.onebusaway.tripplanner.services;

public interface StopTimeProxy {
  
  public Integer getId();

  public String getTripId();
  
  public String getRouteId();

  public int getSequence();

  public StopProxy getStop();

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime();

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime();
}
