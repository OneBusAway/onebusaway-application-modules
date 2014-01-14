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
package org.onebusaway.presentation.impl;

import org.onebusaway.exceptions.InvalidSelectionServiceException;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.model.StopSelectionTreeBean;
import org.onebusaway.presentation.services.LocationNameSplitStrategy;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class StopSelectionServiceImpl implements StopSelectionService {

  private boolean _splitStopNames = false;

  private LocationNameSplitStrategy _locationNameSplitStrategy;

  public void setLocationNameSplitStrategy(
      LocationNameSplitStrategy locationNameSplitStrategy) {
    _locationNameSplitStrategy = locationNameSplitStrategy;
  }

  public StopSelectionBean getSelectedStops(StopsForRouteBean stopsForRoute,
      List<Integer> selectionIndices) throws InvalidSelectionServiceException {
    StopSelectionBean selection = new StopSelectionBean();
    StopSelectionTreeBean tree = getStopsForRouteAsStopSelectionTree(stopsForRoute);
    visitTree(tree, selection, selectionIndices, 0);
    return selection;
  }

  /****
   * Private Methods
   ****/

  private StopSelectionTreeBean getStopsForRouteAsStopSelectionTree(
      StopsForRouteBean stopsForRoute) {

    StopSelectionTreeBean tree = new StopSelectionTreeBean();

    StopGroupingBean byDirection = getGroupingByType(stopsForRoute,
        TransitDataConstants.STOP_GROUPING_TYPE_DIRECTION);

    Map<String, StopBean> stopsById = getStopsById(stopsForRoute);

    if (byDirection != null) {
      groupByDirection(tree, stopsForRoute, byDirection, stopsById);
    } else {
      groupByStop(tree, stopsForRoute.getStops());
    }

    return tree;
  }

  private StopGroupingBean getGroupingByType(StopsForRouteBean stopsForRoute,
      String type) {

    List<StopGroupingBean> groupings = stopsForRoute.getStopGroupings();

    for (StopGroupingBean grouping : groupings) {
      if (grouping.getType().equals(type))
        return grouping;
    }
    return null;
  }

  private void groupByDirection(StopSelectionTreeBean tree,
      StopsForRouteBean stopsForRoute, StopGroupingBean byDirection,
      Map<String, StopBean> stopsById) {

    List<StopGroupBean> groups = byDirection.getStopGroups();

    if (groups.isEmpty()) {
      groupByStop(tree, stopsForRoute.getStops());
      return;
    }

    for (StopGroupBean group : groups) {

      StopSelectionTreeBean subTree = tree.getSubTree(group.getName());
      List<StopBean> stops = new ArrayList<StopBean>();
      for (String stopId : group.getStopIds())
        stops.add(stopsById.get(stopId));
      groupByStop(subTree, stops);
    }
  }

  private void groupByStop(StopSelectionTreeBean tree, Iterable<StopBean> stops) {

    for (StopBean stop : stops) {

      StopSelectionTreeBean subTree = tree;

      if (_splitStopNames) {
        List<NameBean> names = _locationNameSplitStrategy.splitLocationNameIntoParts(stop.getName());
        for (NameBean name : names)
          subTree = subTree.getSubTree(name);
      } else {
        NameBean name = new NameBean(SelectionNameTypes.STOP_NAME,
            stop.getName());
        subTree = subTree.getSubTree(name);
      }

      // As a last resort, we extend the tree by the stop number (guaranteed to
      // be unique)
      String code = stop.getCode() != null ? stop.getCode() : stop.getId();

      NameBean name = new NameBean(SelectionNameTypes.STOP_DESCRIPTION,
          "Stop # " + code);
      subTree = subTree.getSubTree(name);
      subTree.setStop(stop);
    }
  }

  private Map<String, StopBean> getStopsById(StopsForRouteBean stopsForRoute) {
    Map<String, StopBean> stopsById = new HashMap<String, StopBean>();
    for (StopBean stop : stopsForRoute.getStops())
      stopsById.put(stop.getId(), stop);
    return stopsById;
  }

  private void visitTree(StopSelectionTreeBean tree,
      StopSelectionBean selection, List<Integer> selectionIndices, int index)
      throws InvalidSelectionServiceException {

    // If we have a stop, we have no choice but to return
    if (tree.hasStop()) {
      selection.setStop(tree.getStop());
      return;
    }

    Set<NameBean> names = tree.getNames();

    // If we've only got one name, short circuit
    if (names.size() == 1) {

      NameBean next = names.iterator().next();
      selection.addSelected(next);

      StopSelectionTreeBean subtree = tree.getSubTree(next);
      visitTree(subtree, selection, selectionIndices, index);

      return;
    }

    if (index >= selectionIndices.size()) {

      for (NameBean name : names) {
        StopBean stop = getStop(tree.getSubTree(name));
        if (stop != null) {
          selection.addNameWithStop(name, stop);
        } else {
          selection.addName(name);
        }
      }

      List<StopBean> stops = tree.getAllStops();

      for (StopBean stop : stops)
        selection.addStop(stop);

      return;
    } else {

      int i = 0;
      int selectionIndex = selectionIndices.get(index);

      for (NameBean name : names) {
        if (selectionIndex == i) {
          selection.addSelected(name);
          tree = tree.getSubTree(name);
          visitTree(tree, selection, selectionIndices, index + 1);
          return;
        }
        i++;
      }
    }

    // If we made it here...
    throw new InvalidSelectionServiceException();
  }

  private StopBean getStop(StopSelectionTreeBean tree) {

    if (tree.hasStop())
      return tree.getStop();

    if (tree.getNames().size() == 1) {
      NameBean next = tree.getNames().iterator().next();
      return getStop(tree.getSubTree(next));
    }

    return null;
  }
}