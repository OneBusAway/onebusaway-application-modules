package org.onebusaway.tripplanner;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.tripplanner.model.Walk;

public interface WalkPlannerService {
  public Walk getWalkPlan(Point from, Point to) throws NoPathException;

  public Walk getWalkPlan(CoordinatePoint latLonFrom, Point from,
      CoordinatePoint latLonTo, Point to) throws NoPathException;
}
