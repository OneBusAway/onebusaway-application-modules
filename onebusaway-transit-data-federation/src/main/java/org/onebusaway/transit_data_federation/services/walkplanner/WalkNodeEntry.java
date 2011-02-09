package org.onebusaway.transit_data_federation.services.walkplanner;

import org.onebusaway.transit_data_federation.model.ProjectedPoint;

public interface WalkNodeEntry {

  public int getId();

  public ProjectedPoint getLocation();

  public boolean hasEdges();

  public Iterable<WalkEdgeEntry> getEdges();
}
