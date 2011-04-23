package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferTree extends TransferTreeNode {

  private final Pair<StopEntry> stops;

  public TransferTree(Pair<StopEntry> stops) {
    this.stops = stops;
  }
  
  public Pair<StopEntry> getStops() {
    return stops;
  }

  public StopEntry getFromStop() {
    return stops.getFirst();
  }

  public StopEntry getToStop() {
    return stops.getSecond();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    toString(this, "", b);
    return b.toString();
  }
}
