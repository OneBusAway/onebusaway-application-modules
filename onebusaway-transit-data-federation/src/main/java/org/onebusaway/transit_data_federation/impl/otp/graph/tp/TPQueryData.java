package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Map;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.GraphPath;

public class TPQueryData {

  private final Vertex sourceVertex;

  private final Vertex destVertex;

  private final Map<StopEntry, GraphPath> sourceStops;

  private final Map<StopEntry, GraphPath> destStops;

  public TPQueryData(Vertex sourceVertex, Map<StopEntry, GraphPath> sourceStops,
      Vertex destVertex, Map<StopEntry, GraphPath> destStops) {
    this.sourceVertex = sourceVertex;
    this.sourceStops = sourceStops;
    this.destVertex = destVertex;
    this.destStops = destStops;
  }

  public Vertex getSourceVertex() {
    return sourceVertex;
  }

  public Vertex getDestVertex() {
    return destVertex;
  }

  public Map<StopEntry, GraphPath> getSourceStops() {
    return sourceStops;
  }

  public Map<StopEntry, GraphPath> getDestStops() {
    return destStops;
  }
}
