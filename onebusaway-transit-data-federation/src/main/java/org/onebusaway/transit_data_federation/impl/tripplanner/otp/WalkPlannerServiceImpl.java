package org.onebusaway.transit_data_federation.impl.tripplanner.otp;

import java.util.Date;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTEdge;

public class WalkPlannerServiceImpl implements WalkPlannerService {

  private PathService _pathService;

  public void setPathService(PathService pathService) {
    _pathService = pathService;
  }

  @Override
  public WalkPlannerGraph getWalkPlannerGraph() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom,
      CoordinatePoint latLonTo) throws NoPathException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom,
      CoordinatePoint latLonTo, WalkPlannerConstraints constraints)
      throws NoPathException {

    String fromPlace = latLonFrom.getLat() + "," + latLonFrom.getLon();
    String toPlace = latLonTo.getLat() + "," + latLonTo.getLon();
    Date targetTime = new Date();
    TraverseOptions options = new TraverseOptions();
    options.modes = new TraverseModeSet(TraverseMode.WALK);
    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace, targetTime,
        options, 1);
    if (paths == null || paths.isEmpty())
      throw new NoPathException();

    GraphPath path = paths.get(0);
    for (SPTEdge edge : path.edges) {

    }
    // TODO Auto-generated method stub
    return null;
  }

}
