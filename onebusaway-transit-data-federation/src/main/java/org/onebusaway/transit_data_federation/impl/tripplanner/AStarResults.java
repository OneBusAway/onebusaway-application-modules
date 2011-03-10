package org.onebusaway.transit_data_federation.impl.tripplanner;

import edu.washington.cs.rse.collections.MinHeapMap;

public class AStarResults<T extends AStarNode> {

  private MinHeapMap<Double, T> _estimatedDistanceQueue = new MinHeapMap<Double, T>();

  public void putTotalDistanceEstimate(T node, double distance) {
    _estimatedDistanceQueue.add(distance, node);
  }

  public T getMinEstimatedNode() {
    return _estimatedDistanceQueue.removeMinValue();
  }

  public boolean hasOpenNodes() {
    return !_estimatedDistanceQueue.isEmpty();
  }
}
