package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.osm.OSMContentHandler;
import edu.washington.cs.rse.geospatial.osm.OSMLibrary;
import edu.washington.cs.rse.geospatial.osm.OSMNode;
import edu.washington.cs.rse.geospatial.osm.OSMRelation;
import edu.washington.cs.rse.geospatial.osm.OSMWay;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.tripplanner.model.WalkPlannerGraph;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WalkPlannerGraphFactoryBean extends AbstractFactoryBean {

  private File _path;

  private ProjectionService _projection;

  public void setPath(File path) {
    _path = path;
  }

  public void setProjectionService(ProjectionService projection) {
    _projection = projection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class getObjectType() {
    return WalkPlannerGraph.class;
  }

  @Override
  protected Object createInstance() throws Exception {

    System.out.println("======== WalkPlannerGraphFactory =>");

    Handler handler = new Handler();
    OSMLibrary.parseMap(_path, handler);
    WalkPlannerGraph graph = handler.getGraph();
    
    Set<Integer> islands = new HashSet<Integer>();
    
    for( Integer id : graph.getIds() ) {
      if( graph.getNeighbors(id).isEmpty() )
        islands.add(id);
    }
    
    System.out.println("pruning " + islands.size() + " islands");
    graph.removeNodes(islands);
    
    return graph;
  }

  private class Handler implements OSMContentHandler {

    private WalkPlannerGraph _graph = new WalkPlannerGraph();

    private int _nodeCount;

    private int _wayCount;

    public WalkPlannerGraph getGraph() {
      return _graph;
    }

    public void addNode(OSMNode node) {

      if (_nodeCount % 1000 == 0)
        System.out.println("nodes=" + _nodeCount);
      _nodeCount++;

      CoordinatePoint cp = new CoordinatePoint(node.getLat(), node.getLon());
      Point p = _projection.getLatLonAsPoint(node.getLat(), node.getLon());
      _graph.addNode(node.getId(), cp, p);
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
        _graph.addEdge(pair.getFirst(), pair.getSecond());
    }
  }

}
