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

public class NearEdgeWalkState extends AStarNodeImpl implements WalkState {

  private WalkEdgeEntry _edge;

  private ProjectedPoint _location;

  private ProjectedPoint _locationOnEdge;

  private double _distanceToEdge;

  private boolean _forward;

  public NearEdgeWalkState(WalkEdgeEntry edge, ProjectedPoint location,
      ProjectedPoint locationOnEdge, boolean forward) {
    _edge = edge;
    _location = location;
    _locationOnEdge = locationOnEdge;
    _distanceToEdge = _location.distance(_locationOnEdge);
    _forward = forward;
  }

  public boolean isForward() {
    return _forward;
  }

  public WalkEdgeEntry getEdge() {
    return _edge;
  }

  public ProjectedPoint getLocation() {
    return _location;
  }

  public ProjectedPoint getLocationOnEdge() {
    return _locationOnEdge;
  }

  public double getDistanceToLocation() {
    return _distanceToEdge;
  }

  public Collection<WithDistance<WalkState>> getNeighbors(WalkProblem problem,
      Collection<WithDistance<WalkState>> results) {

    results.add(getEdgeToNodeTransition(problem, _edge.getNodeFrom()));
    results.add(getEdgeToNodeTransition(problem, _edge.getNodeTo()));

    return results;
  }

  public void addTransitionFromNodeIfTarget(AtNodeWalkState from,
      Collection<WithDistance<WalkState>> results) {

    if (from.getNode().equals(_edge.getNodeFrom())) {

      double d = _distanceToEdge
          + _location.distance(_edge.getNodeFrom().getLocation());
      results.add(WithDistance.create((WalkState) this, d));

    } else if (from.getNode().equals(_edge.getNodeTo())) {

      double d = _distanceToEdge
          + _location.distance(_edge.getNodeTo().getLocation());
      results.add(WithDistance.create((WalkState) this, d));
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NearEdgeWalkState))
      return false;
    NearEdgeWalkState seg = (NearEdgeWalkState) obj;
    return _edge.equals(seg._edge) && _location.equals(seg._location);
  }

  @Override
  public int hashCode() {
    return _edge.hashCode() + 31 * _location.hashCode();
  }

  @Override
  public String toString() {
    return "nearEdge";
  }

  private WithDistance<WalkState> getEdgeToNodeTransition(WalkProblem problem,
      WalkNodeEntry node) {
    double d = _distanceToEdge + _locationOnEdge.distance(node.getLocation());
    return WithDistance.create((WalkState) problem.getNodeAsState(node), d);
  }

}