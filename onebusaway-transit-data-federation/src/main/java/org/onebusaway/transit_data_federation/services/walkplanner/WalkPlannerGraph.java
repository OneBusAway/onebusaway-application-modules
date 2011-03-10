package org.onebusaway.transit_data_federation.services.walkplanner;

import org.onebusaway.geospatial.model.CoordinateBounds;

import java.util.Collection;

public interface WalkPlannerGraph {
  public abstract Collection<WalkNodeEntry> getNodesByLocation(CoordinateBounds bounds);
}