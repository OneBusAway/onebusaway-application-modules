package org.onebusaway.api.model.transit.tripplanning;

import java.util.List;

public class GraphResultV2Bean {
  private List<VertexV2Bean> vertices;

  private List<EdgeV2Bean> edges;

  public List<VertexV2Bean> getVertices() {
    return vertices;
  }

  public void setVertices(List<VertexV2Bean> vertices) {
    this.vertices = vertices;
  }

  public List<EdgeV2Bean> getEdges() {
    return edges;
  }

  public void setEdges(List<EdgeV2Bean> edges) {
    this.edges = edges;
  }
}
