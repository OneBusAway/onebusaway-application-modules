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
package org.onebusaway.where.impl;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.same.TreeUnionFind;
import edu.washington.cs.rse.collections.same.IUnionFind.Sentry;

import org.onebusaway.common.graph.Graph;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.SelectionNameTypes;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.StopSelectionList;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.services.LocationNameSplitStrategy;
import org.onebusaway.where.services.NoSuchRouteException;
import org.onebusaway.where.services.StopSelectionService;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("unused")
@Component
public class StopSelectionServiceImpl implements StopSelectionService {

  private GtfsDao _gtfsDao;

  private WhereDao _whereDao;

  private LocationNameSplitStrategy _locationNameSplitStrategy;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _gtfsDao = dao;
  }

  @Autowired
  public void setWhereDao(WhereDao dao) {
    _whereDao = dao;
  }

  @Autowired
  public void setLocationNameSplitStrategy(
      LocationNameSplitStrategy locationNameSplitStrategy) {
    _locationNameSplitStrategy = locationNameSplitStrategy;
  }

  @Transactional
  public StopSelectionTree getStopsByRoute(String routeShortName)
      throws NoSuchRouteException {

    Route route = _gtfsDao.getRouteByShortName(routeShortName);

    if (route == null)
      throw new NoSuchRouteException();

    List<StopSequenceBlock> blocks = _whereDao.getStopSequenceBlocksByRoute(route);

    StopSelectionTree tree = new StopSelectionTree();

    for (StopSequenceBlock block : blocks) {

      String dest = block.getDescription();
      SelectionName name = new SelectionName(SelectionNameTypes.DESTINATION,
          dest);
      StopSelectionTree byDest = tree.getSubTree(name);
      handleStopSequences(route, byDest, block.getStopSequences());
    }

    return tree;
  }

  public StopSelectionList getTreeSelection(StopSelectionTree tree,
      List<Integer> selection) throws IndexOutOfBoundsException {
    StopSelectionList list = new StopSelectionList();
    visitTree(tree, list, selection, 0);
    return list;
  }

  /***************************************************************************
   * 
   **************************************************************************/

  private void handleStopSequences(Route route, StopSelectionTree tree,
      List<StopSequence> stopSequences) {

    Graph<Stop> graph = constructStopConnectivityGraphFromStopSequences(stopSequences);

    // Get a topological sort of the stops based on the graph and the comparator
    // for breaking ties
    StopGraphComparator c = new StopGraphComparator(graph);
    List<Stop> ordered = graph.getTopologicalSort(c);

    if (ordered.isEmpty())
      System.err.println("no stops for: route=" + route.getShortName());

    SortedMap<Layer, SortedMap<Integer, Region>> indexedRegionsByLayer = getRegionsByLayerAndStopIndex(ordered);
    
    List<Layer> layers = new ArrayList<Layer>(indexedRegionsByLayer.keySet());

    extendTree(ordered, indexedRegionsByLayer, tree, layers, 0, 0,
        ordered.size());
  }

  /**
   * Construct a connectivity graph, where each node in the graph is a Stop. If
   * two stops are consecutive in a StopSequence, then an edge is drawn between
   * them.
   * 
   * We wish to use the stop graph to create a topological sort of the stops. As
   * such, our graph must be free of cycles, but StopSequences are not cycle
   * free. As such, we do not add any stop edge that creates a cycle. Because we
   * iterate over stop in sequence-order pairs, this typically means the last
   * edge that would complete a cycle is not added.
   * 
   * @param stopSequences
   * @return
   */
  private Graph<Stop> constructStopConnectivityGraphFromStopSequences(
      List<StopSequence> stopSequences) {
    Graph<Stop> graph = new Graph<Stop>();

    for (StopSequence pattern : stopSequences) {

      List<Stop> stops = pattern.getStops();

      Stop prevStop = null;
      for (Stop stop : stops) {
        if (prevStop != null) {

          // We do this to avoid cycles
          if (!graph.isConnected(stop, prevStop))
            graph.addEdge(prevStop, stop);
        }
        prevStop = stop;
      }
    }
    return graph;
  }

  /**
   * Construct a mapping of Regions by Layer and Stop index.
   * 
   * @param ordered
   * @return
   */
  private SortedMap<Layer, SortedMap<Integer, Region>> getRegionsByLayerAndStopIndex(
      List<Stop> ordered) {

    Map<Stop, SortedMap<Layer, Region>> regions = _whereDao.getRegionsByStops(ordered);

    SortedMap<Layer, SortedMap<Integer, Region>> indexedRegionsByLayer = new TreeMap<Layer, SortedMap<Integer, Region>>();

    int stopIndex = 0;

    for (Stop stop : ordered) {

      SortedMap<Layer, Region> regionsByLayer = regions.get(stop);

      if (regionsByLayer == null) {
        System.err.println("no regions for stop: " + stop.getId());
        regionsByLayer = new TreeMap<Layer, Region>();
      }

      for (Map.Entry<Layer, Region> entry : regionsByLayer.entrySet()) {
        Layer layer = entry.getKey();
        Region region = entry.getValue();
        SortedMap<Integer, Region> regionsByIndex = indexedRegionsByLayer.get(layer);
        if (regionsByIndex == null) {
          regionsByIndex = new TreeMap<Integer, Region>();
          indexedRegionsByLayer.put(layer, regionsByIndex);
        }
        regionsByIndex.put(stopIndex, region);
      }
      stopIndex++;
    }
    return indexedRegionsByLayer;
  }

  /**
   * A recursive method for populating the StopSelectionTree.
   * 
   * @param stops - the ordered set of stops (including potential duplicates)
   * @param regionsByLayerAndStopIndex - the set of regions for each stop,
   *          sorted by layer and stop index
   * @param tree - the tree to populate
   * @param layers - the set of layers
   * @param layerIndex
   * @param from
   * @param to
   */
  private void extendTree(List<Stop> stops,
      SortedMap<Layer, SortedMap<Integer, Region>> regionsByLayerAndStopIndex,
      StopSelectionTree tree, List<Layer> layers, int layerIndex, int from,
      int to) {

    // If we are out of layers, proceed to street names
    if (layerIndex >= layers.size()) {
      extendTreeByStreet(tree, stops, from, to);
      return;
    }

    Layer layer = layers.get(layerIndex);

    SortedMap<Integer, Region> regionsByIndex = regionsByLayerAndStopIndex.get(layer);
    regionsByIndex = regionsByIndex.subMap(from, to);
    SelectionName[] namesByIndex = new SelectionName[to - from];

    // If there are no regions for this layer and segment, we can just move
    // on to the next layer
    if (regionsByIndex.isEmpty()) {
      extendTree(stops, regionsByLayerAndStopIndex, tree, layers,
          layerIndex + 1, from, to);
      return;
    }

    for (int index = from; index < to; index++) {
      SelectionName name = getRegionNameOfStopByIndex(regionsByIndex, index);
      namesByIndex[index - from] = name;
    }

    SelectionName last = null;
    int lastIndex = -1;

    for (int index = from; index < to; index++) {

      SelectionName name = namesByIndex[index - from];
      if (name == null)
        throw new IllegalStateException("name shouldn't be null");

      if (last == null) {
        lastIndex = index;
      } else if (!last.equals(name)) {
        StopSelectionTree subtree = tree.getSubTree(last);
        extendTree(stops, regionsByLayerAndStopIndex, subtree, layers,
            layerIndex + 1, lastIndex, index);
        lastIndex = index;
      }
      last = name;
    }

    StopSelectionTree subtree = tree.getSubTree(last);
    extendTree(stops, regionsByLayerAndStopIndex, subtree, layers,
        layerIndex + 1, lastIndex, to);
  }

  private SelectionName getRegionNameOfStopByIndex(
      SortedMap<Integer, Region> regionsByIndex, int stopIndex) {

    Region region = regionsByIndex.get(stopIndex);

    if (region == null) {

      Region before = null;
      SortedMap<Integer, Region> beforeMap = regionsByIndex.headMap(stopIndex);
      if (!beforeMap.isEmpty())
        before = beforeMap.get(beforeMap.lastKey());

      Region after = null;
      SortedMap<Integer, Region> afterMap = regionsByIndex.tailMap(stopIndex);
      if (!afterMap.isEmpty())
        after = afterMap.get(afterMap.firstKey());
      
      SelectionName name = getRegionsAsNameBean(before, after);
      System.err.println(name.getType() + " " + CollectionsLibrary.getObjectsAsString(name.getNames(), " - " ));
      return name;
      
    } else {
      return new SelectionName(SelectionNameTypes.REGION_IN, region.getName());
    }
  }

  private SelectionName getRegionsAsNameBean(Region before, Region after) {
    
    if (before == null && after == null)
      throw new IllegalStateException("shouldn't happen");

    if (before == null)
      return new SelectionName(SelectionNameTypes.REGION_BEFORE,
          after.getName());

    if (after == null)
      return new SelectionName(SelectionNameTypes.REGION_AFTER,
          before.getName());
    
    if( before.equals(after))
      return new SelectionName(SelectionNameTypes.REGION_IN, before.getName());

    return new SelectionName(SelectionNameTypes.REGION_BETWEEN,
        before.getName(), after.getName());
  }

  private void extendTreeByStreet(StopSelectionTree tree, List<Stop> stops,
      int from, int to) {

    for (int i = from; i < to; i++) {

      StopSelectionTree subTree = tree;

      Stop stop = stops.get(i);
      List<SelectionName> names = _locationNameSplitStrategy.splitLocationNameIntoParts(stop.getName());
      for (SelectionName name : names)
        subTree = subTree.getSubTree(name);

      if (stop.getDesc() != null) {
        SelectionName name = new SelectionName(
            SelectionNameTypes.STOP_DESCRIPTION, stop.getDesc());
        subTree = subTree.getSubTree(name);
      }

      // As a last resort, we extend the tree by the stop number (guaranteed to
      // be unique)
      SelectionName name = new SelectionName(
          SelectionNameTypes.STOP_DESCRIPTION, "Stop # " + stop.getId());
      subTree = subTree.getSubTree(name);

      subTree.setStop(stop);
    }
  }

  private void visitTree(StopSelectionTree tree, StopSelectionList bean,
      List<Integer> selection, int index) throws IndexOutOfBoundsException {

    // If we have a stop, we have no choice but to return
    if (tree.hasStop()) {
      bean.setStop(tree.getStop());
      return;
    }

    Set<SelectionName> names = tree.getNames();

    // If we've only got one name, short circuit
    if (names.size() == 1) {

      SelectionName next = names.iterator().next();
      bean.addSelected(next);

      StopSelectionTree subtree = tree.getSubTree(next);
      visitTree(subtree, bean, selection, index);

      return;
    }

    if (index >= selection.size()) {

      for (SelectionName name : names) {
        Stop stop = getStop(tree.getSubTree(name));
        if (stop != null) {
          bean.addNameWithStop(name, stop);
        } else {
          bean.addName(name);
        }
      }
      return;

    } else {

      int i = 0;
      int selectionIndex = selection.get(index);

      for (SelectionName name : names) {
        if (selectionIndex == i) {
          bean.addSelected(name);
          tree = tree.getSubTree(name);
          visitTree(tree, bean, selection, index + 1);
          return;
        }
        i++;
      }
    }

    // If we made it here...
    throw new IndexOutOfBoundsException();
  }

  private Stop getStop(StopSelectionTree tree) {

    if (tree.hasStop())
      return tree.getStop();

    if (tree.getNames().size() == 1) {
      SelectionName next = tree.getNames().iterator().next();
      return getStop(tree.getSubTree(next));
    }

    return null;
  }

  private static class StopGraphComparator implements Comparator<Stop> {

    private Graph<Stop> _graph;

    private Map<Stop, Double> _maxDistance = new HashMap<Stop, Double>();

    public StopGraphComparator(Graph<Stop> graph) {
      _graph = graph;
    }

    public int compare(Stop o1, Stop o2) {
      double d1 = getMaxDistance(o1);
      double d2 = getMaxDistance(o2);
      return d1 == d2 ? 0 : (d1 < d2 ? 1 : -1);
    }

    private double getMaxDistance(Stop stop) {
      return getMaxDistance(stop, new HashSet<Stop>());
    }

    private double getMaxDistance(Stop stop, Set<Stop> visited) {
      Double d = _maxDistance.get(stop);
      if (d != null)
        return d;

      if (!visited.add(stop))
        throw new IllegalStateException("cycle");

      double dMax = 0.0;
      for (Stop next : _graph.getOutboundNodes(stop)) {
        double potential = UtilityLibrary.distance(stop.getLocation(),
            next.getLocation())
            + getMaxDistance(next, visited);
        if (potential > dMax)
          dMax = potential;
      }
      _maxDistance.put(stop, dMax);
      return dMax;
    }
  }

}
