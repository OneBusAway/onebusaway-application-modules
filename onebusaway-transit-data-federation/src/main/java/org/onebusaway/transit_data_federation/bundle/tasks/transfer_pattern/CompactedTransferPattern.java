package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class CompactedTransferPattern implements TransferPattern {

  private final StopEntry[] _stops;

  private final int[] _parentIndices;

  private final Map<StopEntry, int[]> _leafIndicesByStop;

  public CompactedTransferPattern(StopEntry[] stops, int[] parentIndices,
      Map<StopEntry, int[]> leafIndicesByStop) {
    _stops = stops;
    _parentIndices = parentIndices;
    _leafIndicesByStop = leafIndicesByStop;
  }

  /****
   * {@link TransferPattern} Interface
   ****/

  public StopEntry getOriginStop() {
    return _stops[0];
  }

  public Set<StopEntry> getStops() {
    return _leafIndicesByStop.keySet();
  }

  public List<List<Pair<StopEntry>>> getPathsForStop(StopEntry stop) {

    int[] leafIndices = _leafIndicesByStop.get(stop);

    if (leafIndices == null)
      return Collections.emptyList();

    List<List<Pair<StopEntry>>> paths = new ArrayList<List<Pair<StopEntry>>>();

    for (int rootIndex : leafIndices) {

      int leafIndex = rootIndex;

      List<Pair<StopEntry>> path = new ArrayList<Pair<StopEntry>>();
      paths.add(path);
      while (leafIndex >= 0) {
        StopEntry toStop = _stops[leafIndex];
        leafIndex = _parentIndices[leafIndex];
        if (leafIndex < 0)
          throw new IllegalStateException();
        StopEntry fromStop = _stops[leafIndex];
        leafIndex = _parentIndices[leafIndex];
        path.add(0, Tuples.pair(fromStop, toStop));
      }
    }

    return paths;
  }
}
