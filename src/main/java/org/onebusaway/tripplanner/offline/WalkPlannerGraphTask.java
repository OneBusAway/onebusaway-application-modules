package org.onebusaway.tripplanner.offline;

import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.osm.OSMContentHandler;
import edu.washington.cs.rse.geospatial.osm.OSMLibrary;
import edu.washington.cs.rse.geospatial.osm.OSMNode;
import edu.washington.cs.rse.geospatial.osm.OSMRelation;
import edu.washington.cs.rse.geospatial.osm.OSMWay;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.impl.ObjectSerializationLibrary;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.tripplanner.impl.WalkPlannerGraphImpl;
import org.onebusaway.tripplanner.services.WalkPlannerGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WalkPlannerGraphTask implements Runnable {

  private File _inputPath;

  private File _outputPath;

  private ProjectionService _projection;

  public void setOpenStreetMapInputPath(File path) {
    _inputPath = path;
  }

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  public void setProjectionService(ProjectionService projection) {
    _projection = projection;
  }

  public void run() {

    System.out.println("======== WalkPlannerGraphFactory =>");

    try {
      Handler handler = new Handler();
      OSMLibrary.parseMap(_inputPath, handler);
      WalkPlannerGraph graph = handler.getGraph();
      ObjectSerializationLibrary.writeObject(_outputPath, graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error constructing WalkPlannerGraph", ex);
    }
  }

  private class Handler implements OSMContentHandler {

    private Map<Integer, NodeInfo> _nodes = new HashMap<Integer, NodeInfo>();

    private Set<Integer> _nodesWithNeighbors = new HashSet<Integer>();

    private List<Pair<Integer>> _edges = new ArrayList<Pair<Integer>>();

    private int _nodeCount;

    private int _wayCount;

    public WalkPlannerGraph getGraph() {

      // Remove all island
      _nodes.keySet().retainAll(_nodesWithNeighbors);

      WalkPlannerGraphImpl graph = new WalkPlannerGraphImpl(_nodes.size());
      Map<Integer, Integer> idMapping = new HashMap<Integer, Integer>();

      for (Map.Entry<Integer, NodeInfo> entry : _nodes.entrySet()) {
        Integer currentId = entry.getKey();
        NodeInfo info = entry.getValue();
        int newId = graph.addNode(info.getLocationLatLng(), info.getLocation());
        idMapping.put(currentId, newId);
      }

      for (Pair<Integer> edge : _edges) {
        Integer idA = idMapping.get(edge.getFirst());
        Integer idB = idMapping.get(edge.getSecond());
        graph.addEdge(idA, idB);
      }

      return graph;
    }

    public void addNode(OSMNode node) {

      if (_nodeCount % 1000 == 0)
        System.out.println("nodes=" + _nodeCount);
      _nodeCount++;

      CoordinatePoint cp = new CoordinatePoint(node.getLat(), node.getLon());
      Point p = _projection.getLatLonAsPoint(node.getLat(), node.getLon());
      _nodes.put(node.getId(), new NodeInfo(cp, p));
    }

    public void addRelation(OSMRelation relation) {

    }

    public void addWay(OSMWay way) {

      if (_wayCount % 1000 == 0)
        System.out.println("ways=" + _wayCount);
      _wayCount++;

      Map<String, String> tags = way.getTags();
      String value = tags.get("highway");
      if (value == null || value.equals("motorway")
          || value.equals("motorway_link"))
        return;

      for (Pair<Integer> pair : Combinations.getSequentialPairs(way.getNodeRefs())) {
        _edges.add(pair);
        _nodesWithNeighbors.add(pair.getFirst());
        _nodesWithNeighbors.add(pair.getSecond());
      }

    }
  }

  private static class NodeInfo {

    private CoordinatePoint _locationLatLng;

    private Point _location;

    public NodeInfo(CoordinatePoint cp, Point p) {
      _locationLatLng = cp;
      _location = p;
    }

    public CoordinatePoint getLocationLatLng() {
      return _locationLatLng;
    }

    public Point getLocation() {
      return _location;
    }
  }

}
