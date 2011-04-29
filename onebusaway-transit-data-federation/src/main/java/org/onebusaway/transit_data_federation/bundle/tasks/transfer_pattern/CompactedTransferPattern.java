package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class CompactedTransferPattern implements TransferPattern {

  private final short[] _stopIndices;

  private final int[] _parentIndices;

  private final int _exitAllowedOffset;

  private final int _hubOffset;

  private final List<StopEntry> _allStops;

  public CompactedTransferPattern(short[] stopIndices, int[] parentIndices,
      int exitAllowedOffset, int hubOffset, List<StopEntry> allStops) {
    _stopIndices = stopIndices;
    _parentIndices = parentIndices;
    _exitAllowedOffset = exitAllowedOffset;
    _hubOffset = hubOffset;
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
  public Collection<TransferParent> getTransfersForStops(TransferParent root,
      List<StopEntry> stops) {
    
    /**
     * The assumption here is that the stops are in order by increasing stop.getIndex()
     */

    List<TransferParent> nodes = new ArrayList<TransferParent>();

    List<Integer> leafIndices = findLeafIndicesForStops(stops);
    for (int leafIndex : leafIndices) {
      nodes.add(getTransferForLeafIndex(leafIndex, true, root));
    }

    return nodes;
  }

  @Override
  public Collection<TransferParent> getTransfersForAllStops(TransferParent root) {
    List<TransferParent> nodes = new ArrayList<TransferParent>();
    for (int leafIndex = _exitAllowedOffset; leafIndex < _hubOffset; leafIndex++) {
      nodes.add(getTransferForLeafIndex(leafIndex, true, root));
    }
    return nodes;
  }

  @Override
  public Map<StopEntry, List<TransferParent>> getTransfersForHubStops(
      TransferParent root) {

    Map<StopEntry, List<TransferParent>> parentsByHubStop = new FactoryMap<StopEntry, List<TransferParent>>(
        new ArrayList<TransferParent>());

    for (int index = _hubOffset; index < _stopIndices.length; index++) {
      StopEntry hubStop = _allStops.get(_stopIndices[index]);
      int leafIndex = _parentIndices[index];
      TransferParent parent = getTransferForLeafIndex(leafIndex, false, root);
      parentsByHubStop.get(hubStop).add(parent);
    }

    return parentsByHubStop;
  }

  /**
   * @return **
   * 
   ****/

  private List<Integer> findLeafIndicesForStops(List<StopEntry> stops) {
    List<Integer> indices = new ArrayList<Integer>();
    divideAndConquer(_exitAllowedOffset, _hubOffset, stops, 0, stops.size(),
        indices);
    return indices;
  }

  private void divideAndConquer(int stopArrayIndexFrom, int stopArrayIndexTo,
      List<StopEntry> stops, int stopsFrom, int stopsTo, List<Integer> results) {
    
    if( stopsFrom == stopsTo)
      return;

    int mid = (stopsFrom + stopsTo) / 2;
    StopEntry stop = stops.get(mid);
    short stopIndex = (short) stop.getIndex();
    int index = Arrays.binarySearch(_stopIndices, stopArrayIndexFrom,
        stopArrayIndexTo, stopIndex);

    int indexLower = -1;
    int indexUpper = -1;

    if (index >= 0) {
      indexLower = getLowerIndex(index, stopIndex);
      indexUpper = getUpperIndex(index, stopIndex);
      for (int i = indexLower; i < indexUpper; i++)
        results.add(i);
    } else {
      index = -(index + 1);
      indexLower = index;
      indexUpper = index;
    }
    
    if( stopsFrom + 1 >= stopsTo)
      return;

    divideAndConquer(stopArrayIndexFrom, indexLower, stops, stopsFrom, mid,
        results);
    divideAndConquer(indexUpper, stopArrayIndexTo, stops, mid + 1, stopsTo,
        results);
  }

  private int getLowerIndex(int index, short stopIndex) {
    while (index > 0 & _stopIndices[index - 1] == stopIndex)
      index--;
    return index;
  }

  private int getUpperIndex(int index, short stopIndex) {
    while (index < _stopIndices.length && _stopIndices[index] == stopIndex)
      index++;
    return index;
  }

  private TransferParent getTransferForLeafIndex(int leafIndex,
      boolean exitAllowed, TransferParent root) {

    if (leafIndex < 0)
      return root;

    StopEntry toStop = _allStops.get(_stopIndices[leafIndex]);
    int leafIndexB = _parentIndices[leafIndex];
    if (leafIndexB < 0)
      throw new IllegalStateException();
    StopEntry fromStop = _allStops.get(_stopIndices[leafIndexB]);
    int leafIndexC = _parentIndices[leafIndexB];

    TransferParent parent = getTransferForLeafIndex(leafIndexC, false, root);
    
    /**
     * This really shouldn't happen... but it does, so we short circuit the transfer pattern
     */
    if( fromStop == toStop )
      return parent;
    
    return parent.extendTree(fromStop, toStop, exitAllowed);
  }
}
