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
package org.onebusaway.where.phone.actions;

import java.util.ArrayList;
import java.util.List;


import org.onebusaway.where.phone.actions.schedule.PredictedScheduleByStopAction;
import org.onebusaway.where.web.common.client.model.DepartureBean;

public class PredictedScheduleByRouteAction extends
    PredictedScheduleByStopAction {

  private static final long serialVersionUID = 1L;

  private String _route;

  public void setRoute(String route) {
    _route = route;
  }

  public void setArrivals(List<DepartureBean> arrivals) {
    _predictions = arrivals;
  }

  @Override
  public String execute() throws Exception {

    List<DepartureBean> arrivals = new ArrayList<DepartureBean>();

    for (DepartureBean pab : _predictions) {
      if (pab.getRoute().equals(_route))
        arrivals.add(pab);
    }

    _predictions = arrivals;

    return SUCCESS;
  }
}
