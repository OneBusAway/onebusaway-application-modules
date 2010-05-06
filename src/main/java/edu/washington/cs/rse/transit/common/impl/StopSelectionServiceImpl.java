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
package edu.washington.cs.rse.transit.common.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StreetName;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;
import edu.washington.cs.rse.transit.common.model.aggregate.SelectionName;
import edu.washington.cs.rse.transit.common.model.aggregate.SelectionNameTypes;
import edu.washington.cs.rse.transit.common.model.aggregate.StopSelectionList;
import edu.washington.cs.rse.transit.common.model.aggregate.StopSelectionTree;
import edu.washington.cs.rse.transit.common.services.NoSuchRouteException;
import edu.washington.cs.rse.transit.common.services.StopSelectionService;

@Component
public class StopSelectionServiceImpl implements StopSelectionService {

    private MetroKCDAO _dao;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public StopSelectionTree getStopsByRoute(int routeNumber) throws NoSuchRouteException {

        Route route = _dao.getRouteByNumber(routeNumber);

        if (route == null)
            throw new NoSuchRouteException();

        List<ServicePattern> servicePatterns = _dao.getActiveServicePatternsByRoute(route);

        Map<String, List<ServicePattern>> servicePatternsByDestination = new TreeMap<String, List<ServicePattern>>();

        for (ServicePattern pattern : servicePatterns) {
            String dest = pattern.getGeneralDestination();
            List<ServicePattern> patterns = servicePatternsByDestination.get(dest);
            if (patterns == null) {
                patterns = new ArrayList<ServicePattern>();
                servicePatternsByDestination.put(dest, patterns);
            }
            patterns.add(pattern);
        }

        StopSelectionTree tree = new StopSelectionTree();

        for (Map.Entry<String, List<ServicePattern>> entry : servicePatternsByDestination.entrySet()) {
            SelectionName name = new SelectionName(SelectionNameTypes.DESTINATION, entry.getKey());
            StopSelectionTree byDest = tree.getSubTree(name);
            handleServicePatterns(byDest, entry.getValue());
        }

        return tree;
    }

    public StopSelectionList getTreeSelection(StopSelectionTree tree, List<Integer> selection)
            throws IndexOutOfBoundsException {
        StopSelectionList list = new StopSelectionList();
        visitTree(tree, list, selection, 0);
        return list;
    }

    /***************************************************************************
     * 
     **************************************************************************/

    private void handleServicePatterns(StopSelectionTree tree, List<ServicePattern> servicePatterns) {

        for (ServicePattern pattern : servicePatterns) {

            Map<StopLocation, SortedMap<Layer, Region>> stopsAndRegions = _dao
                    .getStopsAndRegionsByServicePattern(pattern);

            Map<Layer, SortedMap<Integer, Region>> indexedRegionsByLayer = new TreeMap<Layer, SortedMap<Integer, Region>>();
            Map<Layer, List<SelectionName>> indexedNamesByLayer = new TreeMap<Layer, List<SelectionName>>();

            int stopIndex = 0;

            for (Map.Entry<StopLocation, SortedMap<Layer, Region>> stopEntry : stopsAndRegions.entrySet()) {

                SortedMap<Layer, Region> regionsByLayer = stopEntry.getValue();

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

            List<StopLocation> stops = new ArrayList<StopLocation>(stopsAndRegions.keySet());
            List<Layer> layers = new ArrayList<Layer>(indexedRegionsByLayer.keySet());

            for (Layer layer : layers) {
                ArrayList<SelectionName> names = new ArrayList<SelectionName>(stops.size());
                for (int i = 0; i < stops.size(); i++)
                    names.add(null);
                indexedNamesByLayer.put(layer, names);
            }

            extendTree(stops, indexedRegionsByLayer, tree, layers, 0, 0, stops.size());
        }
    }

    private void extendTree(List<StopLocation> stops, Map<Layer, SortedMap<Integer, Region>> indexedRegionsByLayer,
            StopSelectionTree tree, List<Layer> layers, int layerIndex, int from, int to) {

        // If we are out of layers, proceed to street names
        if (layerIndex >= layers.size()) {
            extendTreeByStreet(tree, stops, from, to);
            return;
        }

        Layer layer = layers.get(layerIndex);

        SortedMap<Integer, Region> regionsByIndex = indexedRegionsByLayer.get(layer);
        regionsByIndex = regionsByIndex.subMap(from, to);
        SelectionName[] namesByIndex = new SelectionName[to - from];

        // If there are no regions for this layer and segment, we can just move
        // on to the next layer
        if (regionsByIndex.isEmpty()) {
            extendTree(stops, indexedRegionsByLayer, tree, layers, layerIndex + 1, from, to);
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
                extendTree(stops, indexedRegionsByLayer, subtree, layers, layerIndex + 1, lastIndex, index);
                lastIndex = index;
            }
            last = name;
        }

        StopSelectionTree subtree = tree.getSubTree(last);
        extendTree(stops, indexedRegionsByLayer, subtree, layers, layerIndex + 1, lastIndex, to);
    }

    private SelectionName getRegionNameOfStopByIndex(SortedMap<Integer, Region> regionsByIndex, int stopIndex) {

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

            return getRegionsAsNameBean(before, after);
        } else {
            return new SelectionName(SelectionNameTypes.REGION_IN, region.getName());
        }
    }

    private SelectionName getRegionsAsNameBean(Region before, Region after) {

        if (before == null && after == null)
            throw new IllegalStateException("shouldn't happen");

        if (before == null)
            return new SelectionName(SelectionNameTypes.REGION_BEFORE, after.getName());

        if (after == null)
            return new SelectionName(SelectionNameTypes.REGION_AFTER, before.getName());

        return new SelectionName(SelectionNameTypes.REGION_BETWEEN, before.getName(), after.getName());
    }

    private void extendTreeByStreet(StopSelectionTree tree, List<StopLocation> stops, int from, int to) {

        for (int i = from; i < to; i++) {
            StopLocation stop = stops.get(i);
            StreetName main = stop.getMainStreetName();
            SelectionName mainName = new SelectionName(SelectionNameTypes.MAIN_STREET, main.getCombinedName());
            StopSelectionTree mainTree = tree.getSubTree(mainName);

            StreetName cross = stop.getCrossStreetName();
            SelectionName crossName = new SelectionName(SelectionNameTypes.CROSS_STREET, cross.getCombinedName());
            StopSelectionTree crossTree = mainTree.getSubTree(crossName);

            String id = Integer.toString(stop.getId());
            String desc = stop.getDirection();
            SelectionName name = new SelectionName(SelectionNameTypes.STOP_DESCRIPTION, id, desc);
            StopSelectionTree finalTree = crossTree.getSubTree(name);

            finalTree.setStop(stop);
        }
    }

    private void visitTree(StopSelectionTree tree, StopSelectionList bean, List<Integer> selection, int index)
            throws IndexOutOfBoundsException {

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
                StopLocation stop = getStop(tree.getSubTree(name));
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

    private StopLocation getStop(StopSelectionTree tree) {

        if (tree.hasStop())
            return tree.getStop();

        if (tree.getNames().size() == 1) {
            SelectionName next = tree.getNames().iterator().next();
            return getStop(tree.getSubTree(next));
        }

        return null;
    }

}
