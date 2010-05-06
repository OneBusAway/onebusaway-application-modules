package org.onebusaway.transit_data_federation.model.tripplanner;

import java.util.ArrayList;
import java.util.List;

public class TripStateStats extends TripStats {

  private final TripState _state;

  private double _estimatedScore;

  private long _tripStartTime;

  private TripStateStats _parent;

  private List<TripStateStats> _children = new ArrayList<TripStateStats>(2);

  private boolean _excluded = false;

  private final int _hashCode;

  public TripStateStats(double walkingVelocity, TripState state, long tripStartTime, int index) {
    super(walkingVelocity);
    _state = state;
    _tripStartTime = tripStartTime;
    _hashCode = index;
    super.hashCode();
  }

  public TripStateStats(TripStats stats, TripState state, long tripStartTime, int index) {
    super(stats);
    _state = state;
    _tripStartTime = tripStartTime;
    _hashCode = index;
  }

  public TripState getState() {
    return _state;
  }

  public double getEstimatedScore() {
    return _estimatedScore;
  }

  public void setEsimatedScore(double score) {
    _estimatedScore = score;
  }

  public long getTripStartTime() {
    return _tripStartTime;
  }

  public void incrementTripStartTime(long offset) {
    _tripStartTime += offset;
  }

  public TripStateStats getParent() {
    return _parent;
  }

  public void setParent(TripStateStats parent) {
    _parent = parent;
  }

  public void addChild(TripStateStats child) {
    _children.add(child);
  }

  public List<TripStateStats> getChildren() {
    return _children;
  }

  public boolean isExcluded() {
    return _excluded;
  }

  public void setExcluded(boolean excluded) {
    _excluded = excluded;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }
}
