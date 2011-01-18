package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.adapter.IAdapter;
import org.onebusaway.collections.adapter.IterableAdapter;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.serialization.EntryIdAndCallback;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;

public class WalkPlannerGraphImpl implements Serializable, WalkPlannerGraph {

  private static final long serialVersionUID = 1L;

  private static final WalkNodeEntryAdapter _walkNodeEntryAdapter = new WalkNodeEntryAdapter();

  private transient static ReadHelper _helper;

  private List<WalkNodeEntryImpl> _nodes;

  public WalkPlannerGraphImpl() {
    this(0);
  }

  public WalkPlannerGraphImpl(int size) {
    _nodes = new ArrayList<WalkNodeEntryImpl>(size);
  }

  public Iterable<WalkNodeEntry> getNodes() {
    return new IterableAdapter<WalkNodeEntryImpl, WalkNodeEntry>(_nodes,
        _walkNodeEntryAdapter);
  }

  public WalkNodeEntryImpl addNode(int id, ProjectedPoint location) {
    WalkNodeEntryImpl node = new WalkNodeEntryImpl(id, location);
    _nodes.add(node);
    return node;
  }

  public static void handleWalkNodeEntryRead(WalkNodeEntry walkNodeEntry) {
    _helper.handleWalkNodeEntryRead(walkNodeEntry);
  }

  public static void addWalkNodeEntryCallback(Integer entryId,
      EntryCallback<WalkNodeEntry> callback) {
    _helper.addWalkNodeEntryCallback(entryId, callback);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    _helper = new ReadHelper();
    in.defaultReadObject();
    _helper.flush();
    _helper = null;
  }

  private static class ReadHelper {

    private Map<Integer, WalkNodeEntry> _nodes = new HashMap<Integer, WalkNodeEntry>();

    private List<EntryIdAndCallback<Integer, WalkNodeEntry>> _nodeCallbacks = new ArrayList<EntryIdAndCallback<Integer, WalkNodeEntry>>();

    public void handleWalkNodeEntryRead(WalkNodeEntry stopEntryImpl) {
      _nodes.put(stopEntryImpl.getId(), stopEntryImpl);
    }

    public void addWalkNodeEntryCallback(Integer entryId,
        EntryCallback<WalkNodeEntry> callback) {
      _nodeCallbacks.add(new EntryIdAndCallback<Integer, WalkNodeEntry>(
          entryId, callback));
    }

    public void flush() {

      for (EntryIdAndCallback<Integer, WalkNodeEntry> ci : _nodeCallbacks) {
        WalkNodeEntry entry = _nodes.get(ci.getId());
        if (entry == null)
          throw new IllegalStateException("no such WalkNodeEntry: "
              + ci.getId());
        ci.getCallback().handle(entry);
      }
      _nodeCallbacks.clear();
      _nodes.clear();
      _nodeCallbacks = null;
      _nodes = null;

      // Do a GC run, since the graph-reading process requires a lot of data to
      // be loaded
      System.gc();
      System.gc();
    }
  }

  private static class WalkNodeEntryAdapter implements
      IAdapter<WalkNodeEntryImpl, WalkNodeEntry> {

    @Override
    public WalkNodeEntry adapt(WalkNodeEntryImpl source) {
      return source;
    }
  }
}
