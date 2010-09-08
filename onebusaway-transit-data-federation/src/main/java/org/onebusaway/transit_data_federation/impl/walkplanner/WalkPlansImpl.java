package org.onebusaway.transit_data_federation.impl.walkplanner;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.tripplanner.AtLocationState;
import org.onebusaway.transit_data_federation.model.tripplanner.AtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlanSource;

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
      StopEntry stop = ((AtStopState) state).getStop();
      return new StopKey(stop);
    }
    else if( state instanceof AtLocationState) {
      return new LocationKey(((AtLocationState) state).getLocation());
    }
    throw new IllegalStateException("bad");
  }

  private interface Key {
  }

  private static class LocationKey implements Key {

    private final CoordinatePoint _point;

    public LocationKey(CoordinatePoint point) {
      _point = point;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_point == null) ? 0 : _point.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      LocationKey other = (LocationKey) obj;
      return _point.equals(other._point);
    }
  }
  private static class StopKey implements Key {

    private final StopEntry _stop;

    public StopKey(StopEntry stop) {
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
