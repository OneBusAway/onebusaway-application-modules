package org.onebusaway.transit_data_federation.services.transit_graph;

public interface FrequencyEntry {

  public int getStartTime();

  public int getEndTime();

  public int getHeadwaySecs();
}
