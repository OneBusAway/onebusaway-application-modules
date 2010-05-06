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
package edu.washington.cs.rse.transit.phone.actions;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.rse.transit.phone.actions.schedule.PredictedScheduleByStopAction;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PredictedArrivalBean;

public class PredictedScheduleByRouteAction extends
    PredictedScheduleByStopAction {

  private static final long serialVersionUID = 1L;

  private String _route;

  public void setRoute(String route) {
    _route = route;
  }

  public void setArrivals(List<PredictedArrivalBean> arrivals) {
    _predictions = arrivals;
  }

  @Override
  public String execute() throws Exception {

    List<PredictedArrivalBean> arrivals = new ArrayList<PredictedArrivalBean>();

    for (PredictedArrivalBean pab : _predictions) {
      if (pab.getRoute().equals(_route))
        arrivals.add(pab);
    }

    _predictions = arrivals;

    return SUCCESS;
  }
}
