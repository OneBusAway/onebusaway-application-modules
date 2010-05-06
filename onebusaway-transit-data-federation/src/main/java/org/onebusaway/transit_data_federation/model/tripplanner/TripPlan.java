package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlanSource;

import java.util.LinkedList;

public class TripPlan extends TripStats {

  private LinkedList<TripState> _states = new LinkedList<TripState>();

  private WalkPlanSource _walkPlans;

  private long _pendingWaitingTime = 0;

  public TripPlan(WalkPlanSource walkPlans, double walkingVelocity) {
    super(walkingVelocity);
    _walkPlans = walkPlans;
  }

  public TripPlan(TripPlan stats) {
    super(stats);
    _states.addAll(stats._states);
    _walkPlans = stats.getWalkPlans();
  }

  public TripPlan(TripStats stats, WalkPlanSource walkPlans) {
    super(stats);
    _walkPlans = walkPlans;
  }

  public WalkPlanSource getWalkPlans() {
    return _walkPlans;
  }

  public void pushState(TripState state) {
    _states.addFirst(state);
  }

  public void addLastState(TripState state) {
    _states.addLast(state);
  }

  public LinkedList<TripState> getStates() {
    return this._states;
  }

  public void pushPendingWaitingTime(long time) {
    _pendingWaitingTime += time;
  }

  public void flushPendingWaitingTime(boolean isTransferWaitingTime) {
    if (isTransferWaitingTime)
      setTransferWaitingTime(getTransferWaitingTime() + _pendingWaitingTime);
    else
      setInitialWaitingTime(getInitialWaitingTime() + _pendingWaitingTime);
    _pendingWaitingTime = 0;
  }

  public long getTripEndTime() {
    return _states.getLast().getCurrentTime();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (TripState state : _states) {
      if (b.length() > 0)
        b.append(", ");
      b.append(state);
    }
    return b.toString();
  }
}
