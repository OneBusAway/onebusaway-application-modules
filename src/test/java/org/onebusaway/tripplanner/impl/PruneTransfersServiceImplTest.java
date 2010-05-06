package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PruneTransfersServiceImplTest extends BaseTest {

  @Autowired
  private ApplicationContext _context;

  @Test
  public void go() {
    TripPlannerGraph graph = (TripPlannerGraph) _context.getBean("tripPlannerGraph");

    StopEntry stopEntry = graph.getStopEntryByStopId("10020");
    Stop center = stopEntry.getStop();
    Geometry boundary = center.getLocation().buffer(5280).getBoundary();
    List<Stop> stops = graph.getStopsByLocation(boundary);

    Set<Pair<String>> transfers = new HashSet<Pair<String>>();

    for (Stop stop : stops) {
      String id = stop.getId();
      StopEntry entry = graph.getStopEntryByStopId(id);
      for (String to : entry.getTransfers()) {
        Pair<String> pair = Pair.createPair(id, to);
        if (id.compareTo(to) > 0)
          pair = pair.swap();
        transfers.add(pair);
      }
    }

    System.out.println("transfers=" + transfers.size());

    for (Pair<String> transfer : transfers) {
      StopEntry fromEntry = graph.getStopEntryByStopId(transfer.getFirst());
      StopEntry toEntry = graph.getStopEntryByStopId(transfer.getSecond());
      Stop from = fromEntry.getStop();
      Stop to = toEntry.getStop();

      System.out.println(from.getLat() + " " + from.getLon() + " "
          + to.getLat() + " " + to.getLon() + " "
          + UtilityLibrary.distance(from.getLocation(), to.getLocation()));
    }

  }
}
