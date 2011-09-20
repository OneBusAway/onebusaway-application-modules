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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class DirectedGraph<T> {

	private Map<T, Set<T>> _outboundEdges = new HashMap<T, Set<T>>();

	private Map<T, Set<T>> _inboundEdges = new HashMap<T, Set<T>>();

	public DirectedGraph() {

	}

	public DirectedGraph(DirectedGraph<T> graph) {
		for (T node : graph.getNodes())
			addNode(node);
		for (Pair<T> edge : graph.getEdges())
			addEdge(edge.getFirst(), edge.getSecond());
	}

	public Set<T> getNodes() {
		Set<T> nodes = new HashSet<T>();
		nodes.addAll(_outboundEdges.keySet());
		nodes.addAll(_inboundEdges.keySet());
		return nodes;
	}

	public Set<Pair<T>> getEdges() {
		Set<Pair<T>> edges = new HashSet<Pair<T>>();
		for (T from : _outboundEdges.keySet()) {
			for (T to : _outboundEdges.get(from))
				edges.add(Tuples.pair(from, to));
		}
		return edges;
	}

	public Set<T> getInboundNodes(T node) {
		return get(_inboundEdges, node, false);
	}

	public Set<T> getOutboundNodes(T node) {
		return get(_outboundEdges, node, false);
	}

	public boolean isConnected(T from, T to) {

		if (from.equals(to))
			return true;

		return isConnected(from, to, new HashSet<T>());
	}

	public void addNode(T node) {
		get(_outboundEdges, node, true);
		get(_inboundEdges, node, true);
	}

	public void addEdge(T from, T to) {
		get(_outboundEdges, from, true).add(to);
		get(_inboundEdges, to, true).add(from);
	}

	public void removeEdge(T from, T to) {
		get(_outboundEdges, from, false).remove(to);
		get(_inboundEdges, to, false).remove(from);
	}

	private void removeNode(T node) {

		for (T from : get(_inboundEdges, node, false))
			get(_outboundEdges, from, false).remove(node);
		_inboundEdges.remove(node);

		for (T to : get(_outboundEdges, node, false))
			get(_inboundEdges, to, false).remove(node);
		_outboundEdges.remove(node);
	}

	public List<T> getTopologicalSort(Comparator<T> tieBreaker) {

		List<T> order = new ArrayList<T>();
		DirectedGraph<T> g = new DirectedGraph<T>(this);

		while (true) {

			Set<T> nodes = g.getNodes();

			if (nodes.isEmpty())
				return order;

			List<T> noInbound = new ArrayList<T>();

			for (T node : nodes) {
				if (g.getInboundNodes(node).isEmpty())
					noInbound.add(node);
			}

			if (noInbound.isEmpty())
				throw new IllegalStateException("cycle");

			if (tieBreaker != null)
				Collections.sort(noInbound, tieBreaker);

			T node = noInbound.get(0);
			order.add(node);
			g.removeNode(node);
		}
	}

	/****
	 * Private Methods
	 ****/

	private boolean isConnected(T from, T to, Set<T> visited) {

		if (from.equals(to))
			return true;

		for (T next : get(_outboundEdges, from, false)) {
			if (visited.add(next)) {
				if (isConnected(next, to, visited))
					return true;
			}
		}

		return false;
	}

	private Set<T> get(Map<T, Set<T>> edges, T key, boolean create) {
		Set<T> set = edges.get(key);
		if (set == null) {
			set = new HashSet<T>();
			if (create)
				edges.put(key, set);
		}
		return set;
	}
}
