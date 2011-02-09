/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.states;

import org.onebusaway.transit_data_federation.impl.tripplanner.AStarNodeImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.WithDistance;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkProblem;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

import java.util.Collection;

public class NearNodeWalkState extends AStarNodeImpl implements WalkState {

  private WalkNodeEntry _node;

  private double _distanceToLocation;

  private ProjectedPoint _location;

  private boolean _forward;

  public NearNodeWalkState(WalkNodeEntry node, ProjectedPoint location,
      boolean forward) {
    _node = node;
    _location = location;
    _distanceToLocation = _location.distance(_node.getLocation());
    _forward = forward;
  }

  public WalkNodeEntry getNode() {
    return _node;
  }

  public double getDistanceToLocation() {
    return _distanceToLocation;
  }

  public ProjectedPoint getLocation() {
    return _location;
  }

  public boolean isForward() {
    return _forward;
  }

  public Collection<WithDistance<WalkState>> getNeighbors(WalkProblem problem,
      Collection<WithDistance<WalkState>> results) {

    WalkState next = problem.getNodeAsState(_node);
    WithDistance<WalkState> wd = WithDistance.create(next, _distanceToLocation);
    results.add(wd);

    return results;
  }

  public void addTransitionFromNodeIfTarget(AtNodeWalkState from,
      Collection<WithDistance<WalkState>> results) {

    if (from.getNode().equals(_node))
      results.add(WithDistance.create((WalkState) this, _distanceToLocation));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NearNodeWalkState))
      return false;
    NearNodeWalkState seg = (NearNodeWalkState) obj;
    return _node.equals(seg._node) && _location.equals(seg._location);
  }

  @Override
  public int hashCode() {
    return _node.hashCode() + 31 * _location.hashCode();
  }

  @Override
  public String toString() {
    return "nearNode";
  }

}