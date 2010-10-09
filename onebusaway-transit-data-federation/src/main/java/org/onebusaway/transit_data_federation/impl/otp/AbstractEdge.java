package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractEdge implements Edge {

  protected final GraphContext _context;

  public AbstractEdge(GraphContext context) {
    _context = context;
  }

  @Override
  public TraverseMode getMode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDirection() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Geometry getGeometry() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Trip getTrip() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setToVertex(Vertex vertex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFromVertex(Vertex vertex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName(State state) {
    throw new UnsupportedOperationException();
  }
}
