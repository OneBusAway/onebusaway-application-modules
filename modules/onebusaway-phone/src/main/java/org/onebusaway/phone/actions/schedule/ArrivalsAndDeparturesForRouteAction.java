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
package org.onebusaway.phone.actions.schedule;

import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;

import com.opensymphony.xwork2.ActionSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrivalsAndDeparturesForRouteAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _route;

  private StopWithArrivalsAndDeparturesBean _model;

  public void setRoute(String route) {
    _route = route;
  }

  public StopWithArrivalsAndDeparturesBean getModel() {
    return _model;
  }

  public void setModel(StopWithArrivalsAndDeparturesBean model) {
    _model = model;
  }

  @Override
  public String execute() throws Exception {

    Set<String> routeIds = getRouteIdsForMatchingRoutes();

    List<ArrivalAndDepartureBean> arrivals = new ArrayList<ArrivalAndDepartureBean>();

    for (ArrivalAndDepartureBean pab : _model.getArrivalsAndDepartures()) {
      if (routeIds.contains(pab.getRouteId())
          || pab.getRouteShortName().equals(_route)) {
        arrivals.add(pab);
      }
    }

    _model = new StopWithArrivalsAndDeparturesBean(_model.getStop(), arrivals,
        _model.getNearbyStops());

    return SUCCESS;
  }

  private Set<String> getRouteIdsForMatchingRoutes() {
    StopBean stop = _model.getStop();
    Set<String> ids = new HashSet<String>();
    for (RouteBean route : stop.getRoutes()) {
      if (route.getShortName().equals(_route))
        ids.add(route.getId());
    }
    return ids;
  }
}
