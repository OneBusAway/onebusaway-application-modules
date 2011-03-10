package org.onebusaway.transit_data_federation.impl.tripplanner;

import static org.junit.Assert.*;

import org.onebusaway.transit_data_federation.impl.tripplanner.AStarProblem;
import org.onebusaway.transit_data_federation.impl.tripplanner.AStarSearch;
import org.onebusaway.transit_data_federation.impl.tripplanner.WithDistance;
import org.onebusaway.transit_data_federation.impl.tripplanner.AStarSearch.NoPathToGoalException;

import edu.washington.cs.rse.collections.FactoryMap;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AStarSearchTest {

  @Test
  public void testSimple() {

    Node a = new Node(0, "A");
    Node b = new Node(1, "B");
    Node c = new Node(2, "C");
    Node d = new Node(3, "D");

    OneDimensionalDirectedGraph graph = new OneDimensionalDirectedGraph();

    graph.addNode(a, 0);
    graph.addNode(b, 1);
    graph.addNode(c, 2);
    graph.addNode(d, 3);

    graph.addEdge(a, c, 3);
    graph.addEdge(c, d, 2);
    graph.addEdge(a, b, 2);
    graph.addEdge(b, d, 4);

    try {
      Map<Node, Node> path = search(a, d, graph);

      assertEquals("A-C-D", getPathAsString(path, d));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(a, b, graph);
      assertEquals("A-B", getPathAsString(path, b));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(b, d, graph);
      assertEquals("B-D", getPathAsString(path, d));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(a, c, graph);
      assertEquals("A-C", getPathAsString(path, c));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(c, d, graph);
      assertEquals("C-D", getPathAsString(path, d));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      search(d, a, graph);
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      search(d, b, graph);
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      search(d, c, graph);
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      search(c, b, graph);
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      search(c, a, graph);
      fail();
    } catch (NoPathToGoalException e) {
    }

    try {
      search(b, a, graph);
      fail();
    } catch (NoPathToGoalException e) {

    }

  }

  @Test
  public void testLessSimple() {

    Node a = new Node(0, "A");
    Node b = new Node(1, "B");
    Node c = new Node(2, "C");
    Node d = new Node(3, "D");

    OneDimensionalDirectedGraph graph = new OneDimensionalDirectedGraph();
    graph.addNode(a, 0);
    graph.addNode(b, 1);
    graph.addNode(c, 2);
    graph.addNode(d, 3);

    graph.addEdge(a, c, 2);
    graph.addEdge(c, d, 4);
    graph.addEdge(d, c, 3);
    graph.addEdge(a, b, 5);
    graph.addEdge(b, a, 2);
    graph.addEdge(b, d, 2);
    graph.addEdge(d, b, 3);
    graph.addEdge(c, b, 1);
    graph.addEdge(b, c, 1);

    try {
      Map<Node, Node> path = search(a, d, graph);
      assertEquals("A-C-B-D", getPathAsString(path, d));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(d, a, graph);
      assertEquals("D-B-A", getPathAsString(path, a));
    } catch (NoPathToGoalException e) {
      fail();
    }

    try {
      Map<Node, Node> path = search(c, a, graph);
      assertEquals("C-B-A", getPathAsString(path, a));
    } catch (NoPathToGoalException e) {
      fail();
    }
  }

  private String getPathAsString(Map<Node, Node> parentNodes, Node to) {
    Node current = to;
    String path = "";
    while (current != null) {
      if (path.length() > 0)
        path = "-" + path;
      path = current.getValue() + path;
      current = parentNodes.get(current);
    }
    return path;
  }

  private Map<Node, Node> search(Node a, Node d,
      OneDimensionalDirectedGraph graph) throws NoPathToGoalException {
    try {
      return AStarSearch.search(graph, a, d);
    } finally {
      graph.reset();
    }
  }

  private static class OneDimensionalDirectedGraph implements
      AStarProblem<Node> {

    private Map<Node, Double> _nodes = new HashMap<Node, Double>();

    private Map<Node, Map<Node, Double>> _edges = new FactoryMap<Node, Map<Node, Double>>(
        new HashMap<Node, Double>());

    public void addNode(Node node, double location) {
      _nodes.put(node, location);
    }

    public void reset() {
      for (Node node : _nodes.keySet())
        node.reset();
    }

    public void addEdge(Node from, Node to, double distance) {
      _edges.get(from).put(to, distance);
    }

    /*****************************************************************************
     * {@link AStarProblem} Interface
     ****************************************************************************/

    public double getEstimatedDistance(Node from, Node to) {
      double fromPosition = _nodes.get(from);
      double toPosition = _nodes.get(to);
      return Math.abs(toPosition - fromPosition);
    }

    public Collection<WithDistance<Node>> getNeighbors(Node node,
        Collection<WithDistance<Node>> results) {
      Map<Node, Double> nextNodes = _edges.get(node);
      for (Node next : nextNodes.keySet())
        results.add(WithDistance.create(next, nextNodes.get(next)));
      return results;
    }

    public boolean isValid(Node node, double distanceFromStart,
        double estimatedDistanceToEnd) {
      return true;
    }

  }

  private static class Node extends AStarNodeImpl {

    private String _value;

    public Node(int id, String value) {
      super();
      _value = value;
    }

    public String getValue() {
      return _value;
    }

    @Override
    public String toString() {
      return _value;
    }
  }
}
