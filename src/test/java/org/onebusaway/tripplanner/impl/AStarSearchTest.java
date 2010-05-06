package org.onebusaway.tripplanner.impl;

import static org.junit.Assert.*;

import org.onebusaway.tripplanner.impl.AStarSearch.NoPathToGoalException;

import edu.washington.cs.rse.collections.FactoryMap;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AStarSearchTest {

  @Test
  public void testSimple() {

    OneDimensionalDirectedGraph graph = new OneDimensionalDirectedGraph();
    graph.addNode("A", 0);
    graph.addNode("B", 1);
    graph.addNode("C", 2);
    graph.addNode("D", 3);

    graph.addEdge("A", "C", 3);
    graph.addEdge("C", "D", 2);
    graph.addEdge("A", "B", 2);
    graph.addEdge("B", "D", 4);

    try {
      Map<String, String> path = AStarSearch.search(graph, "A", "D");
      assertEquals("A-C-D", getPathAsString(path, "D"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "A", "B");
      assertEquals("A-B", getPathAsString(path, "B"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "B", "D");
      assertEquals("B-D", getPathAsString(path, "D"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "A", "C");
      assertEquals("A-C", getPathAsString(path, "C"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "C", "D");
      assertEquals("C-D", getPathAsString(path, "D"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      AStarSearch.search(graph, "D", "A");
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      AStarSearch.search(graph, "D", "B");
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      AStarSearch.search(graph, "D", "C");
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      AStarSearch.search(graph, "C", "B");
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      AStarSearch.search(graph, "C", "A");
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      AStarSearch.search(graph, "B", "A");
      fail();
    } catch (NoPathToGoalException e) {

    }

  }

  @Test
  public void testLessSimple() {

    OneDimensionalDirectedGraph graph = new OneDimensionalDirectedGraph();
    graph.addNode("A", 0);
    graph.addNode("B", 1);
    graph.addNode("C", 2);
    graph.addNode("D", 3);

    graph.addEdge("A", "C", 2);
    graph.addEdge("C", "D", 4);
    graph.addEdge("D", "C", 3);
    graph.addEdge("A", "B", 5);
    graph.addEdge("B", "A", 2);
    graph.addEdge("B", "D", 2);
    graph.addEdge("D", "B", 3);
    graph.addEdge("C", "B", 1);
    graph.addEdge("B", "C", 1);

    try {
      Map<String, String> path = AStarSearch.search(graph, "A", "D");
      assertEquals("A-C-B-D", getPathAsString(path, "D"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "D", "A");
      assertEquals("D-B-A", getPathAsString(path, "A"));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<String, String> path = AStarSearch.search(graph, "C", "A");
      assertEquals("C-B-A", getPathAsString(path, "A"));
    } catch (NoPathToGoalException e) {
      fail();
    }
  }

  private String getPathAsString(Map<String, String> parentNodes, String to) {
    String current = to;
    String path = "";
    while (current != null) {
      if (path.length() > 0)
        path = "-" + path;
      path = current + path;
      current = parentNodes.get(current);
    }
    return path;
  }

  private static class OneDimensionalDirectedGraph implements AStarProblem<String> {

    private Map<String, Double> _nodes = new HashMap<String, Double>();

    private Map<String, Map<String, Double>> _edges = new FactoryMap<String, Map<String, Double>>(
        new HashMap<String, Double>());

    public void addNode(String id, double location) {
      _nodes.put(id, location);
    }

    public void addEdge(String from, String to, double distance) {
      _edges.get(from).put(to, distance);
    }

    /*****************************************************************************
     * {@link AStarProblem} Interface
     ****************************************************************************/

    public double getDistance(String from, String to) {
      return _edges.get(from).get(to);
    }

    public double getEstimatedDistance(String from, String to) {
      double fromPosition = _nodes.get(from);
      double toPosition = _nodes.get(to);
      return Math.abs(toPosition - fromPosition);
    }

    public Collection<String> getNeighbors(String node, Collection<String> results) {
      return _edges.get(node).keySet();
    }
  }
}
