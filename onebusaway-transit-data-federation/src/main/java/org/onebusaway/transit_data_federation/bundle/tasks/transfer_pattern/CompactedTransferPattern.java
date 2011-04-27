package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class CompactedTransferPattern implements TransferPattern {

  private final short[] _stopIndices;

  private final int[] _parentIndices;

  private final Map<StopEntry, int[]> _leafIndicesByStop;

  private final Map<StopEntry, int[]> _leafIndicesByAccessStop;

  private final List<StopEntry> _allStops;

  public CompactedTransferPattern(short[] stopIndices, int[] parentIndices,
      Map<StopEntry, int[]> leafIndicesByStop,
      Map<StopEntry, int[]> leafIndicesByHubStop, List<StopEntry> allStops) {
    _stopIndices = stopIndices;
    _parentIndices = parentIndices;
    _leafIndicesByStop = leafIndicesByStop;
    _leafIndicesByAccessStop = leafIndicesByHubStop;
    _allStops = allStops;
  }

  /****
   * {@link TransferPattern} Interface
   ****/

  @Override
  public StopEntry getOriginStop() {
    return _allStops.get(_stopIndices[0]);
  }

  @Override
  public Set<StopEntry> getStops() {
    return _leafIndicesByStop.keySet();
  }

  @Override
  public Collection<TransferParent> getTransfersForStop(StopEntry stop,
      TransferParent root) {

    int[] leafIndices = _leafIndicesByStop.get(stop);
    return getTransfersForLeafIndices(root, leafIndices, true);
  }

  @Override
  public Set<StopEntry> getHubStops() {
    return _leafIndicesByAccessStop.keySet();
  }

  @Override
  public Collection<TransferParent> getTransfersForHubStop(StopEntry stop,
      TransferParent root) {

    int[] leafIndices = _leafIndicesByAccessStop.get(stop);
    return getTransfersForLeafIndices(root, leafIndices, false);
  }

  /****
   * 
   ****/

  private List<TransferParent> getTransfersForLeafIndices(TransferParent root,
      int[] leafIndices, boolean exitAllowed) {

    if (leafIndices == null)
      return Collections.emptyList();

    List<TransferParent> nodes = new ArrayList<TransferParent>();
    for (int rootIndex : leafIndices)
      nodes.add(getTransferForLeafIndex(rootIndex, exitAllowed, root));
    return nodes;
  }

  private TransferParent getTransferForLeafIndex(int leafIndex,
      boolean exitAllowed, TransferParent root) {

    if (leafIndex < 0)
      return root;

    StopEntry toStop = _allStops.get(_stopIndices[leafIndex]);
    leafIndex = _parentIndices[leafIndex];
    if (leafIndex < 0)
      throw new IllegalStateException();
    StopEntry fromStop = _allStops.get(_stopIndices[leafIndex]);
    leafIndex = _parentIndices[leafIndex];

    TransferParent parent = getTransferForLeafIndex(leafIndex, false, root);
    return parent.extendTree(fromStop, toStop, exitAllowed);
  }
}
