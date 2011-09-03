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
package org.onebusaway.phone.actions.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.springframework.beans.factory.annotation.Autowired;

public class StopsForRouteAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private StopSelectionService _stopSelectionService;

  private RouteBean _route;

  private NavigationBean _navigation;

  private StopBean _stop;

  @Autowired
  public void setStopSelectionService(StopSelectionService stopSelectionService) {
    _stopSelectionService = stopSelectionService;
  }

  public void setRoute(RouteBean route) {
    _route = route;
  }

  public RouteBean getRoute() {
    return _route;
  }

  public void setNavigation(NavigationBean navigation) {
    _navigation = navigation;
  }

  public NavigationBean getNavigation() {
    return _navigation;
  }

  public StopBean getStop() {
    return _stop;
  }

  @Override
  public String execute() throws Exception {

    StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(_route.getId());
    List<Integer> selectionIndices = Collections.emptyList();
    StopSelectionBean selection = _stopSelectionService.getSelectedStops(
        stopsForRoute, selectionIndices);
    List<NameBean> names = new ArrayList<NameBean>(selection.getNames());

    _navigation = new NavigationBean();
    _navigation.setRoute(_route);
    _navigation.setStopsForRoute(stopsForRoute);
    _navigation.setSelectionIndices(selectionIndices);
    _navigation.setCurrentIndex(0);
    _navigation.setSelection(selection);
    _navigation.setNames(names);

    if (selection.hasStop()) {
      _stop = selection.getStop();
      return "stopFound";
    }

    return SUCCESS;
  }
}
