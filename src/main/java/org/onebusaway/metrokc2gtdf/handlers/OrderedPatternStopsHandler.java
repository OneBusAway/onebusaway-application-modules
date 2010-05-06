package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Max;

import org.onebusaway.metrokc2gtdf.model.MetroKCOrderedPatternStop;
import org.onebusaway.metrokc2gtdf.model.RouteSchedulePatternId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class OrderedPatternStopsHandler extends InputHandler {

  private static String[] ORDERED_PATTERN_STOPS_FIELDS = {
      "sequence", "db_mod_date", "ignore=sequence2", "ignore=sequence3",
      "ppt_flag", "signOfDestination", "signOfDash", "assignedToOns",
      "assignedToOffs", "route", "routePartCode", "showThroughRouteNum",
      "localExpressCode", "schedule_pattern", "directionCode",
      "effective_begin_date", "ignore=patternEventTypeId", "stop"};

  private Map<RouteSchedulePatternId, SortedMap<Date, List<MetroKCOrderedPatternStop>>> _data = new FactoryMap<RouteSchedulePatternId, SortedMap<Date, List<MetroKCOrderedPatternStop>>>(
      new TreeMap<Date, List<MetroKCOrderedPatternStop>>());

  private Set<RouteSchedulePatternId> _activePatterns = new HashSet<RouteSchedulePatternId>();

  public OrderedPatternStopsHandler() {
    super(MetroKCOrderedPatternStop.class, ORDERED_PATTERN_STOPS_FIELDS);
  }

  public Set<Integer> getActiveStops() {
    Set<Integer> activeStops = new HashSet<Integer>();
    for (RouteSchedulePatternId id : _activePatterns) {
      List<MetroKCOrderedPatternStop> opss = getOrderedPatternStopsByRouteSchedulePatternId(id);
      for (MetroKCOrderedPatternStop ops : opss)
        activeStops.add(ops.getStop());
    }
    return activeStops;
  }

  public List<MetroKCOrderedPatternStop> getOrderedPatternStopsByRouteSchedulePatternId(
      RouteSchedulePatternId id) {

    _activePatterns.add(id);

    if (!_data.containsKey(id))
      return new ArrayList<MetroKCOrderedPatternStop>();

    SortedMap<Date, List<MetroKCOrderedPatternStop>> byDate = _data.get(id);

    if (byDate.size() == 1)
      return byDate.get(byDate.lastKey());

    Max<Date> m = new Max<Date>();
    for (Map.Entry<Date, List<MetroKCOrderedPatternStop>> entry : byDate.entrySet()) {
      Date key = entry.getKey();
      List<MetroKCOrderedPatternStop> value = entry.getValue();
      m.add(value.size(), key);
    }
    return byDate.get(m.getMaxElement());
  }

  public void handleEntity(Object bean) {

    MetroKCOrderedPatternStop ops = (MetroKCOrderedPatternStop) bean;
    RouteSchedulePatternId key = ops.getId();

    SortedMap<Date, List<MetroKCOrderedPatternStop>> byDate = _data.get(key);
    List<MetroKCOrderedPatternStop> block = byDate.get(ops.getEffectiveBeginDate());
    if (block == null) {
      block = new ArrayList<MetroKCOrderedPatternStop>();
      byDate.put(ops.getEffectiveBeginDate(), block);
    }

    block.add(ops);
  }

}
