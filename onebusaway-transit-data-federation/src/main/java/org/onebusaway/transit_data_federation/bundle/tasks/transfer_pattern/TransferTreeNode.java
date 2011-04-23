package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferTreeNode {

  private final Map<Pair<StopEntry>, TransferTree> transfers = new HashMap<Pair<StopEntry>, TransferTree>();

  public Collection<TransferTree> getTransfers() {
    return transfers.values();
  }

  public TransferTree extendTree(StopEntry fromStop, StopEntry toStop) {
    Pair<StopEntry> stops = Tuples.pair(fromStop, toStop);
    TransferTree tree = transfers.get(stops);
    if (tree == null) {
      tree = new TransferTree(stops);
      transfers.put(stops, tree);
    }
    return tree;
  }

  public void extendTree(TransferTreeNode tree) {
    transfers.putAll(tree.transfers);
  }

  public double size() {
    if (transfers.isEmpty())
      return 1;
    int size = 0;
    for (TransferTree tree : getTransfers())
      size += tree.size();
    return size;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (TransferTree tree : transfers.values())
      toString(tree, "", b);
    return b.toString();
  }

  protected void toString(TransferTree tree, String prefix, StringBuilder b) {
    b.append(prefix);
    b.append(tree.getFromStop().getId());
    b.append(" ");
    b.append(tree.getToStop().getId());
    b.append("\n");
    for (TransferTree subTree : tree.getTransfers())
      toString(subTree, prefix + "  ", b);
  }
}
