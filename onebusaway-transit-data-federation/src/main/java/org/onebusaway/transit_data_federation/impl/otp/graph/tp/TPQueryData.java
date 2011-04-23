package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPQueryData {

  private final Set<StopEntry> sourceStops;

  private final Set<StopEntry> destStops;

  public TPQueryData(Set<StopEntry> sourceStops, Set<StopEntry> destStops) {
    this.sourceStops = sourceStops;
    this.destStops = destStops;
  }

  public Set<StopEntry> getSourceStops() {
    return sourceStops;
  }

  public Set<StopEntry> getDestStops() {
    return destStops;
  }
}
