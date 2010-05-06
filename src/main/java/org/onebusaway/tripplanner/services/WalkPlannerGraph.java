package org.onebusaway.tripplanner.services;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.util.Set;

public interface WalkPlannerGraph {

  public abstract Set<Integer> getNeighbors(Integer node);

  public abstract Point getLocationById(Integer id);

  public abstract CoordinatePoint getLatLonById(Integer id);

  public abstract Set<Integer> getNodesByLocation(Geometry boundary);

}