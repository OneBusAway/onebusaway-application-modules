/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.twilio.actions.search;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Results({
        @Result(name="stops-for-route-navigation", type="redirectAction", params={"namespace", "/search", "actionName", "stops-for-route-navigation","From", "${phoneNumber}"})
})
public class DirectionsForRouteAction extends AbstractNavigationAction implements SessionAware {

  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(DirectionsForRouteAction.class);
  private RouteBean _route;
  private StopSelectionService _stopSelectionService;

  @Autowired
  public void setStopSelectionService(StopSelectionService stopSelectionService) {
    _stopSelectionService = stopSelectionService;
  }

  public RouteBean getRoute() {
    return _route;
  }

  @Override
  public String execute() throws Exception {
    _log.debug("in DirectionForRoute");

    if (_navigation == null) {
      _navigation = (NavigationBean) sessionMap.get("navigation");
      sessionMap.put("navigation", _navigation);
    }

    // check for direction selection
    if (_navigation != null && _route == null) {
      _route = _navigation.getRoute();
      if (_navigation.getNames().size() > 1 && getInput() != null) {
        // input is direction index
        // load names of those stops and set on _navigation object
        // this is equiv to navigate down
        StopGroupBean stopGroupBean = _navigation.getStopsForRoute().getStopGroupings().get(0).getStopGroups().get(Integer.parseInt(getInput()) - 1);
        _navigation.getStopsForRoute().getStopGroupings().get(0).getStopGroups().clear();
        _navigation.getStopsForRoute().getStopGroupings().get(0).getStopGroups().add(stopGroupBean);

        List<Integer> indices = new ArrayList<Integer>();
        StopSelectionBean selection = _stopSelectionService.getSelectedStops(
                _navigation.getStopsForRoute(), indices);

        List<NameBean> names = new ArrayList<>(selection.getNames());

        _navigation.setNames(names);
        _navigation.setCurrentIndex(0);
        _navigation.setSelection(selection);
        sessionMap.put("navState", new Integer(DISPLAY_DATA));
        return "stops-for-route-navigation";
      }
    }
    buildStopsList();
    return INPUT;

  }
}
