package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;

import org.onebusaway.BaseTest;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateWalkPlannerGraph {
  public static void main(String[] args) {
    ApplicationContext context = BaseTest.getContext();
    // context.getBean("walkPlannerGraph");
    TripPlannerGraph graph = (TripPlannerGraph) context.getBean("tripPlannerGraph");
    
    graph.initialize();
    
    StopEntry en = graph.getStopEntryByStopId("10020");
    Stop stop = en.getStop();
    Geometry boundary = stop.getLocation().buffer(5280).getBoundary();
    List<Stop> stops = graph.getStopsByLocation(boundary);
    Set<Pair<String>> transfers = new HashSet<Pair<String>>();

    for (Stop s : stops) {
      String id = s.getId();
      StopEntry entry = graph.getStopEntryByStopId(id);
      for (String other : entry.getTransfers()) {
        Pair<String> transfer = Pair.createPair(id, other);
        if (id.compareTo(other) > 0)
          transfer = transfer.swap();
        transfers.add(transfer);
      }
    }

    System.out.println("trasnfers=" + transfers.size());

    for (Pair<String> transfer : transfers) {
      StopEntry fromEntry = graph.getStopEntryByStopId(transfer.getFirst());
      StopEntry toEntry = graph.getStopEntryByStopId(transfer.getSecond());

      Stop fromStop = fromEntry.getStop();
      Stop toStop = toEntry.getStop();

      System.out.println(fromStop.getLat() + " " + fromStop.getLon() + " "
          + toStop.getLat() + " " + toStop.getLon());
    }
  }
}
