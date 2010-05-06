package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.impl.OrderedPatternStopsModificationStrategy;
import org.onebusaway.kcmetro2gtfs.model.MetroKCOrderedPatternStop;
import org.onebusaway.kcmetro2gtfs.model.RouteSchedulePatternId;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.emory.mathcs.backport.java.util.Collections;

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

  private List<OrderedPatternStopsModificationStrategy> _modifications = new ArrayList<OrderedPatternStopsModificationStrategy>();

  public OrderedPatternStopsHandler(TranslationContext context) {
    super(MetroKCOrderedPatternStop.class, ORDERED_PATTERN_STOPS_FIELDS);
  }

  public void addModificationStrategy(
      OrderedPatternStopsModificationStrategy modifications) {
    _modifications.add(modifications);
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
    return byDate.get(byDate.lastKey());
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

  @Override
  public void close() {
    super.close();

    // Apply modifications
    for (RouteSchedulePatternId id : _data.keySet()) {
      SortedMap<Date, List<MetroKCOrderedPatternStop>> opssByDate = _data.get(id);

      for (List<MetroKCOrderedPatternStop> opss : opssByDate.values())
        Collections.sort(opss);

      for (OrderedPatternStopsModificationStrategy strategy : _modifications) {
        strategy.modify(id, opssByDate);
      }
    }
  }
}
