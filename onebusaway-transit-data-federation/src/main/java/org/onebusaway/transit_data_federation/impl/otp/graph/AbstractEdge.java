package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.patch.Patch;

public abstract class AbstractEdge implements Edge {

  protected final GraphContext _context;

  public AbstractEdge(GraphContext context) {
    _context = context;
  }

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void addPatch(Patch patch) {

  }

  @Override
  public List<Patch> getPatches() {
    return null;
  }

  @Override
  public void removePatch(Patch patch) {
    
  }
}
