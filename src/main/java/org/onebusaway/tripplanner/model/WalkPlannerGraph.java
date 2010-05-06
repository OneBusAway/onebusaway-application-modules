package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class WalkPlannerGraph implements Serializable {

  private static final long serialVersionUID = 1L;

  private transient RTree _tree = null;

  private Map<Integer, WalkNodeEntry> _entriesById = new HashMap<Integer, WalkNodeEntry>();

  public void initialize() {
    if (_tree == null) {
      _tree = new RTree();
      _tree.init(new Properties());

      for (Map.Entry<Integer, WalkNodeEntry> entry : _entriesById.entrySet()) {
        int id = entry.getKey();
        WalkNodeEntry node = entry.getValue();
        Point point = node.location;
        Rectangle r = new Rectangle((float) point.getX(), (float) point.getY(),
            (float) point.getX(), (float) point.getY());
        _tree.add(r, id);
      }
    }
  }

  public void addNode(Integer id, CoordinatePoint latlon, Point point) {
    WalkNodeEntry node = new WalkNodeEntry();
    node.latLon = latlon;
    node.location = point;
    _entriesById.put(id, node);
  }

  public void removeNodes(Set<Integer> islands) {
    _entriesById.keySet().removeAll(islands);
  }

  public void addEdge(Integer idA, Integer idB) {
    WalkNodeEntry nodeA = _entriesById.get(idA);
    WalkNodeEntry nodeB = _entriesById.get(idB);
    nodeA.neighbors.add(idB);
    nodeB.neighbors.add(idA);
  }

  public Set<Integer> getIds() {
    return _entriesById.keySet();
  }

  public Set<Integer> getNeighbors(Integer node) {
    return _entriesById.get(node).neighbors;
  }

  public Point getLocationById(Integer id) {
    return _entriesById.get(id).location;
  }

  public CoordinatePoint getLatLonById(Integer id) {
    return _entriesById.get(id).latLon;
  }

  public Set<Integer> getNodesByLocation(Geometry boundary) {
    Rectangle r = AbstractGraph.getLocationAsRectangle(boundary);
    Go go = new Go();
    _tree.intersects(r, go);
    return go.getIds();
  }

  private static class Go implements IntProcedure {

    private Set<Integer> _ids = new HashSet<Integer>();

    public Set<Integer> getIds() {
      return _ids;
    }

    public boolean execute(int id) {
      _ids.add(id);
      return true;
    }
  }

  private static class WalkNodeEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    Point location;

    CoordinatePoint latLon;

    Set<Integer> neighbors = new HashSet<Integer>();
  }

}
