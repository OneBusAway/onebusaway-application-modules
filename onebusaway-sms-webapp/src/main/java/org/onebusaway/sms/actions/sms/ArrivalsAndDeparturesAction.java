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
package org.onebusaway.sms.actions.sms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.sms.impl.SmsArrivalsAndDeparturesModel;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class ArrivalsAndDeparturesAction extends AbstractTextmarksAction
    implements ModelDriven<SmsArrivalsAndDeparturesModel> {

  private static final long serialVersionUID = 1L;

  private String[] _args;

  private SmsArrivalsAndDeparturesModel _model;

  @Autowired
  public void setModel(SmsArrivalsAndDeparturesModel model) {
    _model = model;
  }

  public void setStopId(String stopId) {
    _model.setStopIds(Arrays.asList(stopId));
  }

  public void setStopIds(List<String> stopIds) {
    _model.setStopIds(stopIds);
  }

  public void setRouteFilter(Set<String> routeIds) {
    _model.setRouteFilter(routeIds);
  }

  public void setArgs(String[] args) {
    _args = args;
  }

  @Override
  public SmsArrivalsAndDeparturesModel getModel() {
    return _model;
  }

  @Override
  public String execute() throws ServiceException {

    if (_model.isMissingData())
      return INPUT;

    _model.process();


    _model.applyDedupe();
    // Since we have route numbers, not ids, we have to do ad-hoc filtering
    if (_args != null && _args.length > 0)
      filterArrivalsAndDeparturesByRoute(_args);

    return SUCCESS;
  }

  private void filterArrivalsAndDeparturesByRoute(String[] tokens) {

    // Filter by route
    Set<String> routes = new HashSet<String>();

    for (String token : tokens) {
      String[] routeNames = token.split(",");
      for (String routeName : routeNames)
        routes.add(routeName);
    }

    StopsWithArrivalsAndDeparturesBean result = _model.getResult();
    Iterator<ArrivalAndDepartureBean> it = result.getArrivalsAndDepartures().iterator();

    while (it.hasNext()) {
      ArrivalAndDepartureBean bean = it.next();
      TripBean trip = bean.getTrip();
      RouteBean route = trip.getRoute();
      String routeName = RoutePresenter.getNameForRoute(route);
      if (!routes.contains(routeName))
        it.remove();
    }
  }

}
