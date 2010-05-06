package org.onebusaway.transit_data_federation.services.walkplanner;


public interface WalkEdgeEntry {
  public WalkNodeEntry getNodeFrom();

  public WalkNodeEntry getNodeTo();

  public double getDistance();
}
