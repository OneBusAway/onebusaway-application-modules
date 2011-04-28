package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPQueryData {

  private final List<StopEntry> sourceStops;

  private final List<StopEntry> destStops;

  public TPQueryData(Set<StopEntry> sourceStops, Set<StopEntry> destStops) {
    this.sourceStops = new ArrayList<StopEntry>(sourceStops);
    this.destStops = new ArrayList<StopEntry>(destStops);
    Collections.sort(this.sourceStops);
    Collections.sort(this.destStops);
  }

  public List<StopEntry> getSourceStops() {
    return sourceStops;
  }

  public List<StopEntry> getDestStops() {
    return destStops;
  }
}
