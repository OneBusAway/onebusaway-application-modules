package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPQueryData {

  private final Set<StopEntry> destStops;

  public TPQueryData(Set<StopEntry> destStops) {
    this.destStops = destStops;
  }

  public Set<StopEntry> getDestStops2() {
    return destStops;
  }
}
