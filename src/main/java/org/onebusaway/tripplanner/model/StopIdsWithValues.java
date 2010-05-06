package org.onebusaway.tripplanner.model;

public interface StopIdsWithValues {

  public int size();

  public boolean isEmpty();

  public String getStopId(int index);

  public int getValue(int index);
}
