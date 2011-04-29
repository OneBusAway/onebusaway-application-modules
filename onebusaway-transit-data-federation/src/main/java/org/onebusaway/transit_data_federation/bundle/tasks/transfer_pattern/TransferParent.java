package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferParent {

  private final TransferPatternData _data;

  private final Map<Pair<StopEntry>, TransferNode> transfers = new HashMap<Pair<StopEntry>, TransferNode>();

  private final Map<StopEntry, HubNode> hubs = new HashMap<StopEntry, HubNode>();

  public TransferParent(TransferPatternData data) {
    _data = data;
  }

  public Collection<TransferNode> getTransfers() {
    return transfers.values();
  }

  public Collection<HubNode> getHubs() {
    return hubs.values();
  }

  public TransferNode extendTree(StopEntry fromStop, StopEntry toStop,
      boolean exitAllowed) {
    TransferNode node = _data.extendTree(fromStop, toStop, exitAllowed);
    transfers.put(node.getStops(), node);
    return node;
  }

  public void extendHub(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    _data.extendHub(hubStop, stopsTo);
  }

  public void addTransferNode(TransferNode node) {
    transfers.put(node.getStops(), node);
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
      toString(tree, new HashSet<Pair<StopEntry>>(), "", b);
    for (HubNode hub : hubs.values()) {
      b.append(hub.getHubStop().getId());
      b.append(" h\n");
    }
    return b.toString();
  }

  protected void toString(TransferNode tree, Set<Pair<StopEntry>> visited,
      String prefix, StringBuilder b) {
    
    boolean firstVisit = visited.add(tree.getStops());
    
    b.append(prefix);
    b.append(tree.getFromStop().getId());
    b.append(" ");
    b.append(tree.getToStop().getId());
    if (tree.isExitAllowed())
      b.append(" x");
    if (!firstVisit)
      b.append(" ...");
    b.append("\n");
    if (!firstVisit)
      return;
    for (TransferNode subTree : tree.getTransfers())
      toString(subTree, visited, prefix + "  ", b);
    for (HubNode hub : tree.getHubs()) {
      b.append(prefix);
      b.append("  ");
      b.append(hub.getHubStop().getId());
      b.append(" h\n");
    }
  }

}
