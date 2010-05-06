package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.MinHeapMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AStarResults<T> {
  private Set<T> _closed = new HashSet<T>();
  private Set<T> _open = new HashSet<T>();

  private Map<T, Double> _distanceFromStart = new HashMap<T, Double>();
  private Map<T, Double> _estimatedDistanceToEnd = new HashMap<T, Double>();
  private MinHeapMap<Double, T> _estimatedDistanceQueue = new MinHeapMap<Double, T>();

  private Map<T, T> _cameFrom = new HashMap<T, T>();

  public void setClosed(T node) {
    _closed.add(node);
    _open.remove(node);
  }

  public boolean isClosed(T node) {
    return _closed.contains(node);
  }

  public void setOpen(T node) {
    _open.add(node);
  }

  public boolean isOpen(T node) {
    return _open.contains(node);
  }
  
  public boolean hasOpenNodes() {
    return ! _open.isEmpty();
  }

  public void setDistanceFromStart(T node, double distance) {
    _distanceFromStart.put(node, distance);
  }

  public double getDistanceFromStart(T node) {
    return _distanceFromStart.get(node);
  }

  public void setEstimatedDistanceToEnd(T node, double distance) {
    _estimatedDistanceToEnd.put(node, distance);
  }

  public double getEstimatedDistanceToEnd(T node) {
    return _estimatedDistanceToEnd.get(node);
  }

  public void putTotalDistanceEstimate(T node, double distance) {
    _estimatedDistanceQueue.add(distance, node);
  }

  public T getMinEstimatedNode() {
    return _estimatedDistanceQueue.removeMinValue();
  }

  public Map<T, T> getCameFrom() {
    return _cameFrom;
  }
}
