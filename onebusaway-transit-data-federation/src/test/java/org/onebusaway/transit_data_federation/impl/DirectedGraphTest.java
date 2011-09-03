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
package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class DirectedGraphTest {

  @Test
  public void testGetNodes() {

    DirectedGraph<String> graph = new DirectedGraph<String>();
    Set<String> n0 = graph.getNodes();
    assertEquals(0, n0.size());

    graph.addNode("a");
    Set<String> n1 = graph.getNodes();
    assertEquals(1, n1.size());
    assertTrue(n1.contains("a"));

    graph.addNode("b");
    Set<String> n2 = graph.getNodes();
    assertEquals(2, n2.size());
    assertTrue(n2.contains("a"));
    assertTrue(n2.contains("b"));

    graph.addNode("b");
    n2 = graph.getNodes();
    assertEquals(2, n2.size());
    assertTrue(n2.contains("a"));
    assertTrue(n2.contains("b"));

    graph.addNode("c");
    Set<String> n3 = graph.getNodes();
    assertEquals(3, n3.size());
    assertTrue(n3.contains("a"));
    assertTrue(n3.contains("b"));
    assertTrue(n3.contains("c"));
  }

  @Test
  public void testGetEdges() {

    DirectedGraph<String> graph = new DirectedGraph<String>();
    Set<Pair<String>> e0 = graph.getEdges();
    assertEquals(0, e0.size());

    graph.addEdge("a", "b");
    Set<Pair<String>> e1 = graph.getEdges();
    assertEquals(1, e1.size());
    assertTrue(e1.contains(Tuples.pair("a", "b")));

    graph.addEdge("b", "c");
    Set<Pair<String>> e2 = graph.getEdges();
    assertEquals(2, e2.size());
    assertTrue(e2.contains(Tuples.pair("a", "b")));
    assertTrue(e2.contains(Tuples.pair("b", "c")));

    graph.addEdge("b", "c");
    Set<Pair<String>> e2b = graph.getEdges();
    assertEquals(2, e2b.size());
    assertTrue(e2b.contains(Tuples.pair("a", "b")));
    assertTrue(e2b.contains(Tuples.pair("b", "c")));

    graph.addEdge("c", "b");
    Set<Pair<String>> e3 = graph.getEdges();
    assertEquals(3, e3.size());
    assertTrue(e3.contains(Tuples.pair("a", "b")));
    assertTrue(e3.contains(Tuples.pair("b", "c")));
    assertTrue(e3.contains(Tuples.pair("c", "b")));

    graph.removeEdge("b", "c");
    Set<Pair<String>> e4 = graph.getEdges();
    assertEquals(2, e4.size());
    assertTrue(e4.contains(Tuples.pair("a", "b")));
    assertTrue(e4.contains(Tuples.pair("c", "b")));
  }

  @Test
  public void testGetInboundNodes() {

    DirectedGraph<String> graph = new DirectedGraph<String>();
    graph.addNode("b");
    Set<String> n1 = graph.getInboundNodes("b");
    assertEquals(0, n1.size());

    graph.addEdge("a", "b");
    Set<String> n2 = graph.getInboundNodes("b");
    assertEquals(1, n2.size());
    assertTrue(n2.contains("a"));

    Set<String> n3 = graph.getInboundNodes("a");
    assertEquals(0, n3.size());

    graph.addEdge("b", "b");
    Set<String> n4 = graph.getInboundNodes("b");
    assertEquals(2, n4.size());
    assertTrue(n4.contains("a"));
    assertTrue(n4.contains("b"));
  }

  @Test
  public void testGetOutboundNodes() {

    DirectedGraph<String> graph = new DirectedGraph<String>();
    graph.addNode("a");
    Set<String> n1 = graph.getOutboundNodes("a");
    assertEquals(0, n1.size());

    graph.addEdge("a", "b");
    Set<String> n2 = graph.getOutboundNodes("a");
    assertEquals(1, n2.size());
    assertTrue(n2.contains("b"));

    Set<String> n3 = graph.getOutboundNodes("b");
    assertEquals(0, n3.size());

    graph.addEdge("a", "a");
    Set<String> n4 = graph.getOutboundNodes("a");
    assertEquals(2, n4.size());
    assertTrue(n4.contains("a"));
    assertTrue(n4.contains("b"));
  }

  @Test
  public void testIsConnected() {

    DirectedGraph<String> graph = new DirectedGraph<String>();
    graph.addNode("a");
    graph.addNode("b");
    assertFalse(graph.isConnected("a", "b"));

    graph.addEdge("a", "b");
    assertTrue(graph.isConnected("a", "b"));
    assertFalse(graph.isConnected("b", "a"));

    graph.addEdge("b", "c");
    assertTrue(graph.isConnected("a", "b"));
    assertTrue(graph.isConnected("b", "c"));
    assertTrue(graph.isConnected("a", "c"));
    assertFalse(graph.isConnected("b", "a"));
    assertFalse(graph.isConnected("c", "b"));
    assertFalse(graph.isConnected("c", "a"));
  }

  @Test
  public void testGetTopologicalSort() {
    DirectedGraph<String> graph = new DirectedGraph<String>();
    graph.addEdge("a", "b");
    graph.addEdge("b", "c");
    graph.addEdge("c", "d");
    graph.addEdge("d", "e");

    List<String> s1 = graph.getTopologicalSort(null);
    assertEquals(5, s1.size());
    assertEquals(Arrays.asList("a", "b", "c", "d", "e"), s1);
  }
}
