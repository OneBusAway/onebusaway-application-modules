package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.List;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPattern {

  public StopEntry getOriginStop();

  public Set<StopEntry> getStops();

  public List<List<Pair<StopEntry>>> getPathsForStop(StopEntry stop);
}
