/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class MutableTransferPattern implements TransferPattern {

  private final Map<StopEntry, Set<Entry>> _stops = new HashMap<StopEntry, Set<Entry>>();

  private final Entry _root;

  public MutableTransferPattern(StopEntry origin) {
    _root = new Entry(origin, true, null, 0);
  }

  public void addPath(List<Pair<StopEntry>> path) {

    if (path.isEmpty())
      return;

    Entry node = _root;

    for (Pair<StopEntry> segment : path) {
      StopEntry from = segment.getFirst();
      StopEntry to = segment.getSecond();

      if (node == _root) {
        if (from != _root.stop)
          throw new IllegalStateException();
      } else {
        node = node.extend(from);
      }

      node = node.extend(to);
    }

    StopEntry stop = node.stop;
    Set<Entry> nodes = _stops.get(stop);
    if (nodes == null) {
      nodes = new HashSet<Entry>();
      _stops.put(stop, nodes);
    }
    nodes.add(node);
  }

  public long writeTransferPatternsToPrintWriter(PrintWriter out, long index) {
    return writeEntryToPrintWriter(_root, out, index, -1);
  }

  /****
   * {@link TransferPattern} Interface
   ****/

  @Override
  public StopEntry getOriginStop() {
    return _root.stop;
  }

  @Override
  public Collection<TransferParent> getTransfersForStops(TransferParent root,
      List<StopEntry> stops) {

    List<TransferParent> paths = new ArrayList<TransferParent>();

    for (StopEntry stop : stops) {
      Set<Entry> entries = _stops.get(stop);

      if (entries != null) {
        for (Entry entry : entries)
          paths.add(getTransferForEntry(entry, true, root));
      }
    }

    return paths;
  }

  @Override
  public Collection<TransferParent> getTransfersForAllStops(TransferParent root) {

    List<TransferParent> paths = new ArrayList<TransferParent>();

    for (Set<Entry> entries : _stops.values()) {
      for (Entry entry : entries)
        paths.add(getTransferForEntry(entry, true, root));
    }

    return paths;
  }

  @Override
  public Map<StopEntry, List<TransferParent>> getTransfersForHubStops(
      TransferParent root) {
    return Collections.emptyMap();
  }

  /****
   * Private Methods
   *****/

  private TransferParent getTransferForEntry(Entry node, boolean exitAllowed,
      TransferParent root) {

    if (node == null)
      return root;

    Entry b = node;
    Entry a = node.parent;
    if (a == null)
      throw new IllegalStateException();
    node = a.parent;

    TransferParent parent = getTransferForEntry(node, false, root);
    return parent.extendTree(a.stop, b.stop, exitAllowed);
  }

  private long writeEntryToPrintWriter(Entry entry, PrintWriter out,
      long index, long parentIndex) {

    String line = null;
    String stopId = AgencyAndIdLibrary.convertToString(entry.stop.getId());
    Set<Entry> endpoints = _stops.get(entry.stop);
    String endpoint = (endpoints != null && endpoints.contains(entry)) ? "1"
        : "0";

    if (entry.parent != null)
      line = CSVLibrary.getAsCSV(index, stopId, endpoint, parentIndex);
    else
      line = CSVLibrary.getAsCSV(index, stopId, endpoint);

    out.println(line);

    parentIndex = index;
    index++;

    for (Entry child : entry._children.values())
      index = writeEntryToPrintWriter(child, out, index, parentIndex);

    return index;
  }

  private static class Entry {

    private final StopEntry stop;
    private final boolean transfer;
    private final Map<StopEntry, Entry> _children = new HashMap<StopEntry, MutableTransferPattern.Entry>();
    private final Entry parent;
    private final int depth;

    public Entry(StopEntry stop, boolean transfer, Entry parent, int depth) {
      if (stop == null)
        throw new IllegalArgumentException();
      this.stop = stop;
      this.transfer = transfer;
      this.parent = parent;
      this.depth = depth;
    }

    public Entry extend(StopEntry from) {
      Entry node = _children.get(from);
      if (node == null) {
        node = new Entry(from, !this.transfer, this, depth + 1);
        _children.put(from, node);
      }
      return node;
    }

    @Override
    public String toString() {
      return stop.getId() + " " + transfer;
    }
  }
}
