/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.states;

import org.onebusaway.transit_data_federation.impl.tripplanner.AStarNodeImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.WithDistance;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkProblem;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkEdgeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

import java.util.Collection;

public class AtNodeWalkState extends AStarNodeImpl implements WalkState {

  private final WalkNodeEntry _node;

  public AtNodeWalkState(WalkNodeEntry node) {
    _node = node;
  }

  public WalkNodeEntry getNode() {
    return _node;
  }

  public ProjectedPoint getLocation() {
    return _node.getLocation();
  }

  public double getDistanceToLocation() {
    return 0.0;
  }

  public Collection<WithDistance<WalkState>> getNeighbors(WalkProblem problem,
      Collection<WithDistance<WalkState>> results) {

    getNeighboringNodes(problem, results);

    WalkState target = problem.getTarget();
    target.addTransitionFromNodeIfTarget(this, results);

    return results;
  }

  public void addTransitionFromNodeIfTarget(AtNodeWalkState from,
      Collection<WithDistance<WalkState>> results) {
    if (from.getNode().equals(_node))
      results.add(WithDistance.create((WalkState) this, 0));
  }

  /****
   * {@link Object} Methods
   ****/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AtNodeWalkState))
      return false;
    AtNodeWalkState on = (AtNodeWalkState) obj;
    return _node.equals(on._node);
  }

  @Override
  public int hashCode() {
    return _node.hashCode();
  }

  @Override
  public String toString() {
    return "atNode(" + _node.getId() + ")";
  }

  /****
   * Private Methods
   ****/

  private void getNeighboringNodes(WalkProblem problem,
      Collection<WithDistance<WalkState>> results) {
    for (WalkEdgeEntry edge : _node.getEdges()) {
      WalkState next = problem.getNodeAsState(edge.getNodeTo());
      results.add(WithDistance.create(next, edge.getDistance()));
    }
  }
}