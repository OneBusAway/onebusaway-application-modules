/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.states;

import org.onebusaway.transit_data_federation.impl.tripplanner.AStarNode;
import org.onebusaway.transit_data_federation.impl.tripplanner.WithDistance;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkProblem;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;

import java.util.Collection;

public interface WalkState extends AStarNode {
  
  public ProjectedPoint getLocation();

  public double getDistanceToLocation();

  public Collection<WithDistance<WalkState>> getNeighbors(WalkProblem problem, Collection<WithDistance<WalkState>> results);

  public void addTransitionFromNodeIfTarget(AtNodeWalkState from, Collection<WithDistance<WalkState>> results);
}