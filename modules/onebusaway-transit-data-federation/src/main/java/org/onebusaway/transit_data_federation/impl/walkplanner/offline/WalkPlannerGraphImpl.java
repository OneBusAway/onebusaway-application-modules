package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.serialization.EntryCallback;
import org.onebusaway.transit_data_federation.services.serialization.EntryIdAndCallback;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class WalkPlannerGraphImpl implements Serializable, WalkPlannerGraph {

  private static final long serialVersionUID = 1L;

  private transient static ReadHelper _helper;

  private transient RTree _tree = null;

  private List<WalkNodeEntryImpl> _nodes;

  public WalkPlannerGraphImpl(int size) {
    _nodes = new ArrayList<WalkNodeEntryImpl>(size);
  }

  public void initialize() {
    if (_tree == null) {
      _tree = new RTree();
      _tree.init(new Properties());

      for (int i = 0; i < _nodes.size(); i++) {
        WalkNodeEntryImpl node = _nodes.get(i);
        ProjectedPoint loc = node.getLocation();
        float x = (float) loc.getLon();
        float y = (float) loc.getLat();
        Rectangle r = new Rectangle(x, y, x, y);
        _tree.add(r, i);
      }
    }
  }

  public List<WalkNodeEntryImpl> getNodes() {
    return Collections.unmodifiableList(_nodes);
  }

  public WalkNodeEntryImpl addNode(int id, ProjectedPoint location){
    WalkNodeEntryImpl node = new WalkNodeEntryImpl(id, location);
    _nodes.add(node);
    return node;
  }

  public Collection<WalkNodeEntry> getNodesByLocation(CoordinateBounds bounds) {
    Rectangle r = new Rectangle((float) bounds.getMinLon(),
        (float) bounds.getMinLat(), (float) bounds.getMaxLon(),
        (float) bounds.getMaxLat());
    Go go = new Go();
    _tree.intersects(r, go);
    return go.getNodes();
  }

  private class Go implements IntProcedure {

    private List<WalkNodeEntry> _nodesInRange = new ArrayList<WalkNodeEntry>();

    public List<WalkNodeEntry> getNodes() {
      return _nodesInRange;
    }

    public boolean execute(int index) {
      _nodesInRange.add(_nodes.get(index));
      return true;
    }
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
    initialize();
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

}
