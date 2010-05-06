package org.onebusaway.where.web.standard.reroute.client;

public interface PathGraphListener {
  public void handleNodeAdded(PathNode node);

  public void handleNodeMoved(PathNode from, PathNode toNode);

  public void handleNodeRemoved(PathNode node);

  public void handleEdgeAdded(PathEdge edge);

  public void handleEdgeRemoved(PathEdge edge);

}
