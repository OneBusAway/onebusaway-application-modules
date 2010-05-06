/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.web.standard.reroute.client;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathGraph {

  private List<PathGraphListener> _listeners = new ArrayList<PathGraphListener>();

  private Map<PathNode, Set<PathEdge>> _edgesByNode = new HashMap<PathNode, Set<PathEdge>>();

  private Set<PathEdge> _edges = new HashSet<PathEdge>();

  public void addListener(PathGraphListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(PathGraphListener listener) {
    _listeners.remove(listener);
  }

  public PathNode addNode(LatLng location) {
    PathNode node = new PathNode(location);
    if (!_edgesByNode.containsKey(node)) {
      _edgesByNode.put(node, new HashSet<PathEdge>());
      for (PathGraphListener listener : _listeners)
        listener.handleNodeAdded(node);
    }
    return node;
  }

  public void removeNode(PathNode node) {
    if (!_edgesByNode.containsKey(node))
      return;

    Set<PathEdge> edges = new HashSet<PathEdge>(_edgesByNode.get(node));
    for (PathEdge edge : edges)
      removeEdge(edge);
    _edgesByNode.remove(node);
    for (PathGraphListener listener : _listeners)
      listener.handleNodeRemoved(node);
  }

  public PathEdge addEdge(Polyline line) {
    PathNode from = addNode(line.getVertex(0));
    PathNode to = addNode(line.getVertex(line.getVertexCount() - 1));
    return addEdge(from, to, line);
  }

  public PathEdge addEdge(PathNode from, PathNode to, Polyline line) {
    if (!_edgesByNode.containsKey(from) || !_edgesByNode.containsKey(to))
      throw new IllegalStateException("unknown node");
    PathEdge edge = new PathEdge(from, to, line);
    _edges.add(edge);
    _edgesByNode.get(from).add(edge);
    _edgesByNode.get(to).add(edge);
    for (PathGraphListener listener : _listeners)
      listener.handleEdgeAdded(edge);
    return edge;
  }

  public void removeEdge(PathEdge edge) {
    if (!_edges.contains(edge))
      throw new IllegalStateException("unknown node");
    if (!_edgesByNode.containsKey(edge.getFrom()) || !_edgesByNode.containsKey(edge.getTo()))
      throw new IllegalStateException("unknown node");

    _edges.remove(edge);
    _edgesByNode.get(edge.getFrom()).remove(edge);
    _edgesByNode.get(edge.getTo()).remove(edge);
    for (PathGraphListener listener : _listeners)
      listener.handleEdgeRemoved(edge);
  }

  public PathNode moveNode(PathNode from, LatLng to, Map<PathEdge, Polyline> updatedEdges) {

    if (!_edgesByNode.containsKey(from))
      throw new IllegalStateException("unknown node");

    PathNode toNode = new PathNode(to);
    if (!_edgesByNode.containsKey(toNode))
      _edgesByNode.put(toNode, new HashSet<PathEdge>());

    Set<PathEdge> edges = _edgesByNode.get(from);
    if (!edges.equals(updatedEdges.keySet()))
      throw new IllegalStateException("updated edges mismatch");

    for (Map.Entry<PathEdge, Polyline> entry : updatedEdges.entrySet()) {
      PathEdge edge = entry.getKey();
      removeEdge(edge);
      PathNode edgeNode = edge.getOppositeEndPoint(from);
      addEdge(edgeNode, toNode, entry.getValue());
    }

    _edgesByNode.remove(from);

    for (PathGraphListener listener : _listeners)
      listener.handleNodeMoved(from, toNode);

    return toNode;
  }

  public void clear() {
    Set<PathEdge> edges = new HashSet<PathEdge>(_edges);
    for (PathEdge edge : edges)
      removeEdge(edge);
    Set<PathNode> nodes = new HashSet<PathNode>(_edgesByNode.keySet());
    for (PathNode node : nodes)
      removeNode(node);
  }

  public Set<PathEdge> getEdgesForNode(PathNode node) {
    return new HashSet<PathEdge>(_edgesByNode.get(node));
  }
}
