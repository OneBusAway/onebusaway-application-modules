package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.File;
import java.util.UUID;

import org.onebusaway.collections.Counter;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class HubIncrementalTask extends AbstractHubTask {

  @Override
  public void run() {

    Counter<StopEntry> counts = countStops();

    File path = _bundle.getHubStopsPath();

    path = new File(path.getParentFile(), path.getName() + "-Incremental-"
        + UUID.randomUUID().toString());

    writeCountsToPath(counts, path);
  }
}
