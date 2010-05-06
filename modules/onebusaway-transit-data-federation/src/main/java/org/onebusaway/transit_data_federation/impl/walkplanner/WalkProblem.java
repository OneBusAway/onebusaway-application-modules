/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner;

import org.onebusaway.transit_data_federation.impl.tripplanner.AStarProblem;
import org.onebusaway.transit_data_federation.impl.tripplanner.WithDistance;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.AtNodeWalkState;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.WalkState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

import cern.colt.map.OpenIntObjectHashMap;

import java.util.Collection;

public class WalkProblem implements AStarProblem<WalkState> {

  private WalkPlannerConstraints _constraints;

  private WalkState _target;

  private OpenIntObjectHashMap _nodeMapping = new OpenIntObjectHashMap();

  public WalkProblem(WalkPlannerConstraints constraints, WalkState target) {
    _constraints = constraints;
    _target = target;
  }

  public WalkState getNodeAsState(WalkNodeEntry node) {
    WalkState state = (WalkState) _nodeMapping.get(node.getId());
    if (state == null) {
      state = new AtNodeWalkState(node);
      _nodeMapping.put(node.getId(), state);
    }
    return state;
  }

  public WalkState getTarget() {
    return _target;
  }

  /****
   * {@link AStarProblem} Interface
   ****/

  public double getEstimatedDistance(WalkState from, WalkState to) {
    return from.getLocation().distance(to.getLocation());
  }

  public Collection<WithDistance<WalkState>> getNeighbors(WalkState state,
      Collection<WithDistance<WalkState>> results) {
    return state.getNeighbors(this, results);
  }

  public boolean isValid(WalkState node, double distanceFromStart,
      double estimatedDistanceToEnd) {

    if (_constraints == null || !_constraints.hasMaxTripLength())
      return true;

    return distanceFromStart + estimatedDistanceToEnd <= _constraints.getMaxTripLength();
  }
}