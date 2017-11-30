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
package org.onebusaway.enterprise.webapp.actions.where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.utility.text.NaturalStringOrder;

public class StopsForRouteAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  private static final StopNameComparator _stopNameComparator = new StopNameComparator();

  private String _id;

  private RouteBean _route;

  private List<StopBean> _stops = new ArrayList<StopBean>();

  private List<NameBean> _directionNames = new ArrayList<NameBean>();

  private int _groupIndex = -1;

  public void setId(String id) {
    _id = id;
  }

  public void setGroupIndex(int groupIndex) {
    _groupIndex = groupIndex;
  }

  public RouteBean getRoute() {
    return _route;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public List<NameBean> getDirectionNames() {
    return _directionNames;
  }

  @Override
  @Actions( {
      @Action(value = "/where/iphone/stops-for-route")
  })
  public String execute() throws ServiceException {

    if (_id == null || _id.length() == 0)
      return INPUT;

    _route = _transitDataService.getRouteForId(_id);
    StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(_id);

    Map<String, StopGroupingBean> groupingsByType = MappingLibrary.mapToValue(
        stopsForRoute.getStopGroupings(), "type", String.class);
    StopGroupingBean byDirection = groupingsByType.get(TransitDataConstants.STOP_GROUPING_TYPE_DIRECTION);

    if (_groupIndex == -1) {
      if (byDirection != null) {
        for (StopGroupBean group : byDirection.getStopGroups())
          _directionNames.add(group.getName());
      }
      _stops = stopsForRoute.getStops();
      Collections.sort(_stops, _stopNameComparator);
    } else {
      if (byDirection == null)
        return INPUT;
      List<StopGroupBean> groups = byDirection.getStopGroups();
      if (_groupIndex < 0 && groups.size() <= _groupIndex)
        return INPUT;
      Map<String, StopBean> stopById = MappingLibrary.mapToValue(
          stopsForRoute.getStops(), "id", String.class);

      StopGroupBean stopGroup = groups.get(_groupIndex);
      _stops = new ArrayList<StopBean>();
      for (String stopId : stopGroup.getStopIds())
        _stops.add(stopById.get(stopId));
    }
    return SUCCESS;
  }

  private static class StopNameComparator implements Comparator<StopBean> {

    @Override
    public int compare(StopBean o1, StopBean o2) {
      return NaturalStringOrder.compareNatural(o1.getName(), o2.getName());
    }
  }
}
