/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlannerServiceImpl;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.springframework.beans.factory.annotation.Autowired;

class StopWalkPlanCache {

  @Autowired
  private WalkPlannerService _walkPlanner;

  private Map<Pair<StopEntry>, WalkPlan> _cache = new HashMap<Pair<StopEntry>, WalkPlan>();

  private int _cacheHits = 0;

  private int _totalHits = 0;

  public void setWalkPlannerGraph(WalkPlannerGraph graph) {
    WalkPlannerServiceImpl service = new WalkPlannerServiceImpl();
    service.setWalkPlannerGraph(graph);
    service.setTripPlannerConstants(new TripPlannerConstants());
    _walkPlanner = service;
  }

  public int getCacheHits() {
    return _cacheHits;
  }

  public int getTotalHits() {
    return _totalHits;
  }

  public WalkPlan getWalkPlanForStopToStop(Pair<StopEntry> pair)
      throws NoPathException {

    _totalHits++;

    if (_cache.containsKey(pair)) {
      _cacheHits++;
      WalkPlan plan = _cache.get(pair);
      if (plan == null)
        throw new NoPathException();
      return plan;
    }

    StopEntry from = pair.getFirst();
    StopEntry to = pair.getSecond();
    
    try {
      WalkPlan plan = _walkPlanner.getWalkPlan(from.getStopLocation(),
          to.getStopLocation());
      _cache.put(pair, plan);
      return plan;
    } catch (NoPathException ex) {
      _cache.put(pair, null);
      throw ex;
    }
  }
}