package org.onebusaway.transit_data_federation.impl.tripplanner;

public class AStarNodeImpl implements AStarNode {

  private boolean _open = false;

  private boolean _closed = false;

  private double _distanceFromStart;

  private double _estimatedDistanceToEnd;

  private Object _cameFrom = null;

  public void setOpen() {
    _closed = false;
    _open = true;
  }

  public boolean isOpen() {
    return _open;
  }

  public void setClosed() {
    _open = false;
    _closed = true;
  }

  public boolean isClosed() {
    return _closed;
  }

  public void setDistanceFromStart(double distance) {
    _distanceFromStart = distance;
  }

  public double getDistanceFromStart() {
    return _distanceFromStart;
  }

  public void setEstimatedDistanceToEnd(double distance) {
    _estimatedDistanceToEnd = distance;
  }

  public double getEstimatedDistanceToEnd() {
    return _estimatedDistanceToEnd;
  }

  public void setCameFrom(Object cameFrom) {
    _cameFrom = cameFrom;
  }

  public Object getCameFrom() {
    return _cameFrom;
  }

  public void reset() {
    _open = false;
    _closed = false;
    _distanceFromStart = 0.0;
    _estimatedDistanceToEnd = 0.0;
    _cameFrom = null;
  }
}
