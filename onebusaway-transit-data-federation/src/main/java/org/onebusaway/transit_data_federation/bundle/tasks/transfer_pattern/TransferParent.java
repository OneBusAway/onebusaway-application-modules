package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferParent {

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
      tree = new TransferNode(stops);
      transfers.put(stops, tree);
    }
    if (exitAllowed)
      tree.setExitAllowed(exitAllowed);
    return tree;
  }

  public void extendTransferNode(TransferNode node) {
    transfers.put(node.getStops(), node);
  }

  public void extendHub(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    HubNode hubNode = hubs.get(hubStop);
    if (hubNode == null) {
      hubNode = new HubNode(hubStop, stopsTo);
      hubs.put(hubStop, hubNode);
    }
  }

  public int size() {
    if (transfers.isEmpty())
      return 1;
    int size = 0;
    for (TransferNode tree : getTransfers())
      size += tree.size();
    return size;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (TransferNode tree : transfers.values())
      toString(tree, "", b);
    for (HubNode hub : hubs.values()) {
      b.append(hub.getHubStop().getId());
      b.append(" h\n");
    }
    return b.toString();
  }

  protected void toString(TransferNode tree, String prefix, StringBuilder b) {
    b.append(prefix);
    b.append(tree.getFromStop().getId());
    b.append(" ");
    b.append(tree.getToStop().getId());
    if (tree.isExitAllowed())
      b.append(" x");
    b.append("\n");
    for (TransferNode subTree : tree.getTransfers())
      toString(subTree, prefix + "  ", b);
    for (HubNode hub : tree.getHubs()) {
      b.append(prefix);
      b.append("  ");
      b.append(hub.getHubStop().getId());
      b.append(" h\n");
    }
  }

}
