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
package org.onebusaway.phone.actions.stops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.phone.impl.PhoneArrivalsAndDeparturesModel;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;

public class ArrivalsAndDeparturesForRouteAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _route;

  private PhoneArrivalsAndDeparturesModel _model;

  public void setRoute(String route) {
    _route = route;
  }

  public PhoneArrivalsAndDeparturesModel getModel() {
    return _model;
  }

  public void setModel(PhoneArrivalsAndDeparturesModel model) {
    _model = model;
  }

  @Override
  public String execute() throws Exception {

    Set<String> routeIds = getRouteIdsForMatchingRoutes();

    List<ArrivalAndDepartureBean> arrivals = new ArrayList<ArrivalAndDepartureBean>();

    StopsWithArrivalsAndDeparturesBean m = _model.getResult();
    for (ArrivalAndDepartureBean pab : m.getArrivalsAndDepartures()) {
      RouteBean route = pab.getTrip().getRoute();
      if (routeIds.contains(route.getId())
          || _route.equals(route.getShortName())) {
        arrivals.add(pab);
      }
    }

    m = new StopsWithArrivalsAndDeparturesBean(m.getStops(), arrivals,
        m.getNearbyStops(), m.getSituations());

    _model.setResult(m);

    return SUCCESS;
  }

  private Set<String> getRouteIdsForMatchingRoutes() {
    StopsWithArrivalsAndDeparturesBean result = _model.getResult();
    Set<String> ids = new HashSet<String>();
    for (StopBean stop : result.getStops()) {
      for (RouteBean route : stop.getRoutes()) {
        if (route.getShortName().equals(_route))
          ids.add(route.getId());
      }
    }
    return ids;
  }
}
