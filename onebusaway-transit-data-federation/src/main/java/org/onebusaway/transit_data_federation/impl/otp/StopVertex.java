package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public final class StopVertex extends AbstractVertex implements HasEdges {

  private final StopEntry _stop;

  public StopVertex(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public String getStopId() {
    return _stop.getId().toString();
  }

  @Override
  public double getX() {
    return _stop.getStopLon();
  }

  @Override
  public double getY() {
    return _stop.getStopLat();
  }

  @Override
  public int getDegreeIn() {
    return 1;
  }

  @Override
  public Collection<Edge> getIncoming() {
    return Arrays.asList((Edge) new ArrivalEdge(_context, _stop));
  }

  @Override
  public int getDegreeOut() {
    return 1;
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new DepartureEdge(_context, _stop));
  }
}
