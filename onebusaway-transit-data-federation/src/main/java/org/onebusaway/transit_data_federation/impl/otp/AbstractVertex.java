package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractVertex implements Vertex {

  protected final GraphContext _context;

  public AbstractVertex(GraphContext context) {
    _context = context;
  }

  @Override
  public String getLabel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double fastDistance(Vertex v) {
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
}
