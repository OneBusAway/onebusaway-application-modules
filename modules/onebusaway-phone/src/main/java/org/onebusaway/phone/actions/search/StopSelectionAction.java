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
package org.onebusaway.phone.actions.search;

import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class StopSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private StopSelectionService _stopSelectionService;

  private List<Integer> _selection = new ArrayList<Integer>();

  private StopSelectionBean _names;

  private StopBean _stop;

  private RouteBean _route;

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

  public void setSelection(List<Integer> selection) {
    _selection = selection;
  }

  public List<Integer> getSelection() {
    return _selection;
  }

  public StopSelectionBean getNames() {
    return _names;
  }

  public StopBean getStop() {
    return _stop;
  }

  @Override
  public String execute() throws Exception {

    StopsForRouteBean stopsForRouteBean = _transitDataService.getStopsForRoute(_route.getId());

    _names = _stopSelectionService.getSelectedStops(stopsForRouteBean,
        _selection);

    return SUCCESS;
  }
}
