package org.onebusaway.transit_data_federation.services.walkplanner;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;

public interface WalkPlannerService {

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, CoordinatePoint latLonTo) throws NoPathException;

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, CoordinatePoint latLonTo, WalkPlannerConstraints constraints)
      throws NoPathException;
}
