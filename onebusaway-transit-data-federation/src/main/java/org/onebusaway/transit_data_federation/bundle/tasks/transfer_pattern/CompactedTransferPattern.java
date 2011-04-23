package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class CompactedTransferPattern implements TransferPattern {

  private final StopEntry[] _stops;

  private final int[] _parentIndices;

  private final Map<StopEntry, int[]> _leafIndicesByStop;

  private final Map<StopEntry, int[]> _leafIndicesByAccessStop;

  public CompactedTransferPattern(StopEntry[] stops, int[] parentIndices,
      Map<StopEntry, int[]> leafIndicesByStop,
      Map<StopEntry, int[]> leafIndicesByHubStop) {
    _stops = stops;
    _parentIndices = parentIndices;
    _leafIndicesByStop = leafIndicesByStop;
    _leafIndicesByAccessStop = leafIndicesByHubStop;
  }

  /****
   * {@link TransferPattern} Interface
   ****/

  @Override
  public StopEntry getOriginStop() {
    return _stops[0];
  }

  @Override
  public Set<StopEntry> getStops() {
    return _leafIndicesByStop.keySet();
  }

  @Override
  public Collection<TransferTreeNode> getTransfersForStop(StopEntry stop,
      TransferTreeNode root) {

    int[] leafIndices = _leafIndicesByStop.get(stop);
    return getTransfersForLeafIndices(root, leafIndices);
  }

  @Override
  public Set<StopEntry> getHubStops() {
    return _leafIndicesByAccessStop.keySet();
  }

  @Override
  public Collection<TransferTreeNode> getTransfersForHubStop(StopEntry stop,
      TransferTreeNode root) {

    int[] leafIndices = _leafIndicesByAccessStop.get(stop);
    return getTransfersForLeafIndices(root, leafIndices);
  }

  /**
   * @return **
   * 
   ****/

  private List<TransferTreeNode> getTransfersForLeafIndices(
      TransferTreeNode root, int[] leafIndices) {

    if (leafIndices == null)
      return Collections.emptyList();

    List<TransferTreeNode> nodes = new ArrayList<TransferTreeNode>();
    for (int rootIndex : leafIndices)
      nodes.add(getTransferForLeafIndex(rootIndex, root));
    return nodes;
  }

  private TransferTreeNode getTransferForLeafIndex(int leafIndex,
      TransferTreeNode root) {

    if (leafIndex < 0)
      return root;

    StopEntry toStop = _stops[leafIndex];
    leafIndex = _parentIndices[leafIndex];
    if (leafIndex < 0)
      throw new IllegalStateException();
    StopEntry fromStop = _stops[leafIndex];
    leafIndex = _parentIndices[leafIndex];

    TransferTreeNode parent = getTransferForLeafIndex(leafIndex, root);
    return parent.extendTree(fromStop, toStop);
  }
}
