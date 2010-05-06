package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.handlers.OrderedPatternStopsHandler;
import org.onebusaway.metrokc2gtfs.model.MetroKCOrderedPatternStop;
import org.onebusaway.metrokc2gtfs.model.RouteSchedulePatternId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class OrderedPatternStopsModificationFactory implements ModificationFactory {

  public void register(List<Map<String, String>> configs, TranslationContext context) {

    final OpsImpl modifications = new OpsImpl();

    for (Map<String, String> config : configs) {

      OPSMod mod = new OPSMod();

      mod.route = config.get("route");
      mod.beforeStop = config.get("beforeStop");
      mod.afterStop = config.get("afterStop");
      mod.addStop = config.get("addStop");

      if (mod.route == null || mod.beforeStop == null || mod.afterStop == null || mod.addStop == null)
        throw new IllegalStateException("invalid config: " + config);

      modifications.addMod(mod);
    }

    context.addContextListener(new TranslationContextListener() {
      public void onHandlerRegistered(Class<?> type, Object handler) {
        if (type.equals(OrderedPatternStopsHandler.class)) {
          OrderedPatternStopsHandler opsHandler = (OrderedPatternStopsHandler) handler;
          opsHandler.addModificationStrategy(modifications);
        }
      }
    });
  }

  private class OpsImpl implements OrderedPatternStopsModificationStrategy {

    private List<OPSMod> _mods = new ArrayList<OPSMod>();

    public void addMod(OPSMod mod) {
      _mods.add(mod);
    }

    public void modify(RouteSchedulePatternId routeSchedulePatternId,
        SortedMap<Date, List<MetroKCOrderedPatternStop>> orderedPatternsStopsByDate) {

      for (OPSMod mod : _mods) {

        if (mod.route != null && !mod.route.equals(routeSchedulePatternId.getRoute()))
          continue;

        for (Date date : orderedPatternsStopsByDate.keySet()) {
          List<MetroKCOrderedPatternStop> opss = orderedPatternsStopsByDate.get(date);

          int beforeIndex = findStop(opss, mod.beforeStop);
          int afterIndex = findStop(opss, mod.afterStop);

          if (beforeIndex == -1 || afterIndex == -1)
            continue;
          if (beforeIndex + 1 != afterIndex)
            throw new IllegalStateException("before and after stops are not sequential: mod=" + mod + " before="
                + beforeIndex + " after=" + afterIndex);

          MetroKCOrderedPatternStop beforeOps = opss.get(beforeIndex);

          MetroKCOrderedPatternStop newOps = new MetroKCOrderedPatternStop();
          newOps.setDbModDate(beforeOps.getDbModDate());
          newOps.setEffectiveBeginDate(newOps.getEffectiveBeginDate());
          newOps.setId(beforeOps.getId());
          newOps.setPptFlag(false);
          newOps.setSequence(beforeOps.getSequence());
          newOps.setStop(Integer.parseInt(mod.addStop));

          System.out.println("inserting new stop in ordered pattern sequence: mod=" + mod + " before=" + beforeIndex
              + " after=" + afterIndex);
          opss.add(afterIndex, newOps);

          int index = 0;
          for (MetroKCOrderedPatternStop ops : opss)
            ops.setSequence(index++);
        }
      }
    }

    private int findStop(List<MetroKCOrderedPatternStop> opss, String stopId) {
      int index = 0;
      for (MetroKCOrderedPatternStop ops : opss) {
        if (Integer.toString(ops.getStop()).equals(stopId))
          return index;
        index++;
      }
      return -1;
    }
  }

  private static class OPSMod {
    String route;
    String beforeStop;
    String afterStop;
    String addStop;

    @Override
    public String toString() {
      return "(route=" + route + " beforeStop=" + beforeStop + " afterStop=" + afterStop + " addStop=" + addStop + ")";
    }
  }
}
