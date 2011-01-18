package org.onebusaway.transit_data_federation.services.walkplanner;

public interface WalkPlannerGraph {
  public Iterable<WalkNodeEntry> getNodes();
}