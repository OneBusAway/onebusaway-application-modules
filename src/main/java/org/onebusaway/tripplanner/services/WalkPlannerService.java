package org.onebusaway.tripplanner.services;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.tripplanner.model.WalkPlan;

public interface WalkPlannerService {
  public WalkPlan getWalkPlan(Point from, Point to) throws NoPathException;

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, Point from,
      CoordinatePoint latLonTo, Point to) throws NoPathException;
}
