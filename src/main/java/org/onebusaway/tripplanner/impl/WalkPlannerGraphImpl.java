package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.tripplanner.model.AbstractGraph;
import org.onebusaway.tripplanner.services.WalkPlannerGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class WalkPlannerGraphImpl implements Serializable, WalkPlannerGraph {

  private static final long serialVersionUID = 1L;

  private transient RTree _tree = null;

  private List<WalkNodeEntry> _nodes;

  public WalkPlannerGraphImpl(int size) {
    _nodes = new ArrayList<WalkNodeEntry>(size);
  }

  public void initialize() {
    if (_tree == null) {
      _tree = new RTree();
      _tree.init(new Properties());

      for (WalkNodeEntry node : _nodes) {
        int id = node.id;
        Point point = node.location;
        Rectangle r = new Rectangle((float) point.getX(), (float) point.getY(),
            (float) point.getX(), (float) point.getY());
        _tree.add(r, id);
      }
    }
  }

  public int addNode(CoordinatePoint latlon, Point point) {
    WalkNodeEntry node = new WalkNodeEntry();
    node.id = _nodes.size();
    node.latLon = latlon;
    node.location = point;
    _nodes.add(node);
    return node.id;
  }

  public void addEdge(Integer idA, Integer idB) {
    _nodes.get(idA).neighbors.add(idB);
    _nodes.get(idB).neighbors.add(idA);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.onebusaway.tripplanner.impl.WalkPlannerGraph#getNeighbors(java.lang
   * .Integer)
   */
  public Set<Integer> getNeighbors(Integer node) {
    return _nodes.get(node).neighbors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.onebusaway.tripplanner.impl.WalkPlannerGraph#getLocationById(java.lang
   * .Integer)
   */
  public Point getLocationById(Integer id) {
    return _nodes.get(id).location;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.onebusaway.tripplanner.impl.WalkPlannerGraph#getLatLonById(java.lang
   * .Integer)
   */
  public CoordinatePoint getLatLonById(Integer id) {
    return _nodes.get(id).latLon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.onebusaway.tripplanner.impl.WalkPlannerGraph#getNodesByLocation(com
   * .vividsolutions.jts.geom.Geometry)
   */
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

    int id;

    Point location;

    CoordinatePoint latLon;

    Set<Integer> neighbors = new HashSet<Integer>();
  }

}
