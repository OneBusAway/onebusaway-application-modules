package org.onebusaway.transit_data_federation.services.walkplanner;

import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public interface WalkPlannerService {

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, CoordinatePoint latLonTo) throws NoPathException;

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, CoordinatePoint latLonTo, WalkPlannerConstraints constraints)
      throws NoPathException;
}
