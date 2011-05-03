package org.onebusaway.transit_data_federation.services.blocks;


public interface HasIndexedFrequencyBlockTrips {

  public int getStartTimeForIndex(int index);

  public int getEndTimeForIndex(int index);
}
