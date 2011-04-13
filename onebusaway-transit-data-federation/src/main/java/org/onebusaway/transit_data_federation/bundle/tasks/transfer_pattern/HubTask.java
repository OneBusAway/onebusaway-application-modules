package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.File;

import org.onebusaway.collections.Counter;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class HubTask extends AbstractHubTask {

  @Override
  public void run() {
    Counter<StopEntry> counts = countStops();
    File path = _bundle.getHubStopsPath();
    writeCountsToPath(counts, path);
  }
}
