/**
 * 
 */
package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.WalkPlannerService;

import edu.washington.cs.rse.collections.tuple.Pair;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

class StopWalkPlanCache {

  @Autowired
  private WalkPlannerService _walkPlanner;

  private Map<Pair<StopProxy>, WalkPlan> _cache = new HashMap<Pair<StopProxy>, WalkPlan>();

  private int _cacheHits = 0;

  private int _totalHits = 0;

  public int getCacheHits() {
    return _cacheHits;
  }

  public int getTotalHits() {
    return _totalHits;
  }

  public WalkPlan getWalkPlanForStopToStop(Pair<StopProxy> pair) throws NoPathException {

    _totalHits++;

    if (_cache.containsKey(pair)) {
      _cacheHits++;
      WalkPlan plan = _cache.get(pair);
      if (plan == null)
        throw new NoPathException();
      return plan;
    }

    StopProxy from = pair.getFirst();
    StopProxy to = pair.getSecond();

    try {
      WalkPlan plan = _walkPlanner.getWalkPlan(from.getStopLocation(), to.getStopLocation());
      _cache.put(pair, plan);
      return plan;
    } catch (NoPathException ex) {
      _cache.put(pair, null);
      throw ex;
    }
  }
}