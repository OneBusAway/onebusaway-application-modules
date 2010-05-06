package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.AtStopState;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.WalkPlanSource;

import com.vividsolutions.jts.geom.Point;

import java.util.HashMap;
import java.util.Map;

public class WalkPlansImpl implements WalkPlanSource {

  private Map<PairKey, WalkPlan> _walkPlans = new HashMap<PairKey, WalkPlan>();

  private Map<PairKey, Double> _walkDistances = new HashMap<PairKey, Double>();

  public void putWalkPlan(TripState from, TripState to, WalkPlan plan) {
    PairKey pair = constructKey(from, to);
    _walkPlans.put(pair, plan);
  }

  public void putWalkDistance(TripState from, TripState to, double distance) {
    PairKey pair = constructKey(from, to);
    _walkDistances.put(pair, distance);
  }

  public double getWalkDistance(TripState from, TripState to) {
    PairKey pair = constructKey(from, to);
    Double distance = _walkDistances.get(pair);
    if (distance != null)
      return distance.doubleValue();
    WalkPlan plan = _walkPlans.get(pair);
    if (plan != null)
      return plan.getDistance();

    throw new IllegalArgumentException("no walk distance defined for state pair=" + pair);
  }

  public WalkPlan getWalkPlan(TripState from, TripState to) {
    PairKey pair = constructKey(from, to);
    return _walkPlans.get(pair);
  }

  public boolean hasWalkDistance(TripState from, TripState to) {
    PairKey pair = constructKey(from, to);
    return _walkDistances.containsKey(pair) || _walkPlans.containsKey(pair);
  }

  public boolean hasWalkPlan(TripState from, TripState to) {
    PairKey pair = constructKey(from, to);
    return _walkPlans.containsKey(pair);
  }

  private PairKey constructKey(TripState from, TripState to) {
    Key a = constructKey(from);
    Key b = constructKey(to);
    return new PairKey(a, b);
  }

  private Key constructKey(TripState state) {
    if (state instanceof AtStopState) {
      StopProxy stop = ((AtStopState) state).getStop();
      return new StopKey(stop);
    }
    return new LocationKey(state.getLocation());
  }

  private interface Key {
  }

  private static class LocationKey implements Key {
    private double _x;
    private double _y;

    public LocationKey(Point point) {
      _x = point.getX();
      _y = point.getY();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(_x);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(_y);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (!(obj instanceof LocationKey))
        return false;
      LocationKey other = (LocationKey) obj;
      if (Double.doubleToLongBits(_x) != Double.doubleToLongBits(other._x))
        return false;
      if (Double.doubleToLongBits(_y) != Double.doubleToLongBits(other._y))
        return false;
      return true;
    }

  }
  private static class StopKey implements Key {

    private final StopProxy _stop;

    public StopKey(StopProxy stop) {
      _stop = stop;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopKey))
        return false;
      StopKey other = (StopKey) obj;
      return _stop.equals(other._stop);
    }

    @Override
    public int hashCode() {
      return _stop.hashCode();
    }

  }

  private static class PairKey {

    private Key _a;
    private Key _b;

    public PairKey(Key a, Key b) {
      _a = a;
      _b = b;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof PairKey))
        return false;
      PairKey other = (PairKey) obj;
      return _a.equals(other._a) && _b.equals(other._b);
    }

    @Override
    public int hashCode() {
      return _a.hashCode() + _b.hashCode();
    }
  }
}
