package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class HubNode {

  private final StopEntry hubStop;

  private final Iterable<StopEntry> stopsTo;

  public HubNode(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    this.hubStop = hubStop;
    this.stopsTo = stopsTo;
  }

  public StopEntry getHubStop() {
    return hubStop;
  }

  public Iterable<StopEntry> getStopsTo() {
    return stopsTo;
  }

  @Override
  public String toString() {
    return "Hub(" + hubStop.getId() + ")";
  }
}
