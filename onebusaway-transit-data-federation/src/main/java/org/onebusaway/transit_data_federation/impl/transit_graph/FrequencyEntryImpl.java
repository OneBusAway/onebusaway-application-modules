package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyEntryImpl implements FrequencyEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private final int startTime;
  private final int endTime;
  private final int headwaySecs;

  public FrequencyEntryImpl(int startTime, int endTime, int headwaySecs) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.headwaySecs = headwaySecs;
  }

  @Override
  public int getStartTime() {
    return startTime;
  }

  @Override
  public int getEndTime() {
    return endTime;
  }

  @Override
  public int getHeadwaySecs() {
    return headwaySecs;
  }

  @Override
  public String toString() {
    return "startTime=" + startTime + " endTime=" + endTime + " headwaySecs"
        + headwaySecs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + endTime;
    result = prime * result + headwaySecs;
    result = prime * result + startTime;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FrequencyEntryImpl other = (FrequencyEntryImpl) obj;
    if (endTime != other.endTime)
      return false;
    if (headwaySecs != other.headwaySecs)
      return false;
    if (startTime != other.startTime)
      return false;
    return true;
  }
}
