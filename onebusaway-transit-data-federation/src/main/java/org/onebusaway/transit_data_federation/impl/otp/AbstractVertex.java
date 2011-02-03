package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractVertex implements Vertex, TransitVertex {

  protected final GraphContext _context;

  public AbstractVertex(GraphContext context) {
    _context = context;
  }

  @Override
  public String getLabel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double distance(Vertex v) {
    return SphericalGeometryLibrary.distanceFaster(getY(), getX(), v.getY(),
        v.getX());
  }

  @Override
  public double distance(Coordinate c) {
    return SphericalGeometryLibrary.distance(getY(), getX(), c.y, c.x);
  }

  @Override
  public Coordinate getCoordinate() {
    return new Coordinate(getX(), getY());
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDistanceToNearestTransitStop(double distance) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDistanceToNearestTransitStop() {
    // TODO Auto-generated method stub
    return 0;
  }
}