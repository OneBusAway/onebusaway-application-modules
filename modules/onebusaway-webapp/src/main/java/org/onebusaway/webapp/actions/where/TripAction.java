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
package org.onebusaway.webapp.actions.where;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.springframework.beans.factory.annotation.Autowired;

public class TripAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private TripDetailsBean _tripStatus;

  public void setId(String id) {
    _id = id;
  }

  public void setStop(String stopId) {

  }

  public TripDetailsBean getResult() {
    return _tripStatus;
  }

  @Override
  @Actions( {@Action(value = "/where/standard/trip")})
  public String execute() throws ServiceException {

    _tripStatus = _service.getTripDetails(_id);

    return SUCCESS;
  }

  public String getEscapedRouteName(TripDetailsBean bean) {
    TripBean trip = bean.getTrip();
    String routeName = trip.getRouteShortName();
    while (routeName.length() < 3)
      routeName = "0" + routeName;
    return routeName;
  }
}
