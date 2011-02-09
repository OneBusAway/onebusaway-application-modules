package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.PriorityQueue;

public class AStarResults<T extends AStarNode> {

  private PriorityQueue<NodeWithDistance<T>> _estimatedDistanceQueue = new PriorityQueue<NodeWithDistance<T>>();

  public void putTotalDistanceEstimate(T node, double distance) {
    _estimatedDistanceQueue.add(new NodeWithDistance<T>(node, distance));
  }

  public T getMinEstimatedNode() {
    NodeWithDistance<T> node = _estimatedDistanceQueue.remove();
    return node.getNode();
  }

  public boolean hasOpenNodes() {
    return !_estimatedDistanceQueue.isEmpty();
  }

  private static class NodeWithDistance<T> implements
      Comparable<NodeWithDistance<T>> {

    private final T _node;

    private final double _distance;

    public NodeWithDistance(T node, double distance) {
      _node = node;
      _distance = distance;
    }

    public T getNode() {
      return _node;
    }

    @Override
    public int compareTo(NodeWithDistance<T> o) {
      if (_distance == o._distance)
        return 0;
      return _distance < o._distance ? -1 : 1;
    }
  }
}
