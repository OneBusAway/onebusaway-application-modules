package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferPatternData {

  private final Map<Pair<StopEntry>, TransferNode> transfers = new HashMap<Pair<StopEntry>, TransferNode>();

  private final Map<StopEntry, HubNode> hubs = new HashMap<StopEntry, HubNode>();

  public Collection<TransferNode> getTransfers() {
    return transfers.values();
  }

  public Collection<HubNode> getHubs() {
    return hubs.values();
  }

  public TransferNode extendTree(StopEntry fromStop, StopEntry toStop,
      boolean exitAllowed) {
    Pair<StopEntry> stops = Tuples.pair(fromStop, toStop);
    TransferNode tree = transfers.get(stops);
    if (tree == null) {
      tree = new TransferNode(this, stops);
      transfers.put(stops, tree);
    }
    if (exitAllowed)
      tree.setExitAllowed(exitAllowed);
    return tree;
  }

  public void extendHub(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    HubNode hubNode = hubs.get(hubStop);
    if (hubNode == null) {
      hubNode = new HubNode(hubStop, stopsTo);
      hubs.put(hubStop, hubNode);
    }
  }
}
