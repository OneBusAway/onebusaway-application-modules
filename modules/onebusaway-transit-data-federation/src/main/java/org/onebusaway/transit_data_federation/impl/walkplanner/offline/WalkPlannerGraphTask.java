package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;

import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;
import edu.washington.cs.rse.geospatial.osm.OSMContentHandler;
import edu.washington.cs.rse.geospatial.osm.OSMLibrary;
import edu.washington.cs.rse.geospatial.osm.OSMNode;
import edu.washington.cs.rse.geospatial.osm.OSMRelation;
import edu.washington.cs.rse.geospatial.osm.OSMWay;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WalkPlannerGraphTask implements RunnableWithOutputPath {

  private GtfsRelationalDao _gtfsDao;

  private OSMDownloader _openStreetMapDownloader;

  private File _outputPath;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setOpenStreetMapDownloader(OSMDownloader downloader) {
    _openStreetMapDownloader = downloader;
  }

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  public void run() {

    System.out.println("======== WalkPlannerGraphFactory =>");

    try {

      Handler handler = new Handler();

      for (Stop stop : _gtfsDao.getAllStops()) {
        CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
            stop.getLat(), stop.getLon(), 3000);
        CoordinateRectangle r = new CoordinateRectangle(bounds.getMinLat(),
            bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon());
        _openStreetMapDownloader.visitRegion(r, handler);
      }

      WalkPlannerGraph graph = handler.getGraph();
      ObjectSerializationLibrary.writeObject(_outputPath, graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error constructing WalkPlannerGraph", ex);
    }
  }

  private class Handler implements OSMDownloaderListener, OSMContentHandler {

    private Set<String> _visitedMapTiles = new HashSet<String>();

    private Map<Integer, NodeInfo> _nodes = new HashMap<Integer, NodeInfo>();

    private List<Pair<Integer>> _edges = new ArrayList<Pair<Integer>>();

    private int _nodeCount;

    private int _wayCount;

    public WalkPlannerGraph getGraph() {

      Set<Integer> nodesWithNeighbors = new HashSet<Integer>();

      for (Pair<Integer> edges : _edges) {
        if (_nodes.containsKey(edges.getFirst())
            && _nodes.containsKey(edges.getSecond())) {
          nodesWithNeighbors.add(edges.getFirst());
          nodesWithNeighbors.add(edges.getSecond());
        }
      }

      // Remove all island
      _nodes.keySet().retainAll(nodesWithNeighbors);

      WalkPlannerGraphImpl graph = new WalkPlannerGraphImpl(_nodes.size());
      Map<Integer, WalkNodeEntryImpl> idMapping = new HashMap<Integer, WalkNodeEntryImpl>();

      for (Map.Entry<Integer, NodeInfo> entry : _nodes.entrySet()) {
        Integer currentId = entry.getKey();
        NodeInfo info = entry.getValue();
        ProjectedPoint location = ProjectedPointFactory.forward(info.getLocationLatLng());
        WalkNodeEntryImpl walkNodeEntry = graph.addNode(currentId, location);
        idMapping.put(currentId, walkNodeEntry);
      }

      for (Pair<Integer> edge : _edges) {
        WalkNodeEntryImpl nodeA = idMapping.get(edge.getFirst());
        WalkNodeEntryImpl nodeB = idMapping.get(edge.getSecond());
        if (nodeA != null && nodeB != null) {
          double distance = nodeA.getLocation().distance(nodeB.getLocation());
          nodeA.addEdge(nodeB, distance);
          nodeB.addEdge(nodeA, distance);
        }
      }

      return graph;
    }

    public void handleMapTile(String key, double lat, double lon,
        File pathToMapTile) {
      if (_visitedMapTiles.add(key))
        try {
          OSMLibrary.parseMap(pathToMapTile, this);
        } catch (Exception ex) {
          throw new IllegalStateException(ex);
        }
    }

    public void addNode(OSMNode node) {

      if (_nodeCount % 1000 == 0)
        System.out.println("nodes=" + _nodeCount);
      _nodeCount++;

      CoordinatePoint cp = new CoordinatePoint(node.getLat(), node.getLon());
      _nodes.put(node.getId(), new NodeInfo(cp));
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

      for (Pair<Integer> pair : Combinations.getSequentialPairs(way.getNodeRefs()))
        _edges.add(pair);
    }

  }

  private static class NodeInfo {

    private CoordinatePoint _locationLatLng;

    public NodeInfo(CoordinatePoint cp) {
      _locationLatLng = cp;
    }

    public CoordinatePoint getLocationLatLng() {
      return _locationLatLng;
    }
  }

}
