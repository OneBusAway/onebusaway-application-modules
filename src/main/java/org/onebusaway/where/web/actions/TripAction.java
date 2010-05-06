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
package org.onebusaway.where.web.actions;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.model.TripStatusBean;
import org.onebusaway.where.web.common.client.rpc.WhereService;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

public class TripAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private WhereService _whereService;

  private String _id;

  private TripStatusBean _tripStatus;

  public void setId(String id) {
    _id = id;
  }

  public void setStop(String stopId) {

  }

  public TripStatusBean getResult() {
    return _tripStatus;
  }

  @Override
  public String execute() throws ServiceException {

    if (_id == null || _id.length() == 0)
      return INPUT;

    _tripStatus = _whereService.getTripStatus(_id);

    return SUCCESS;
  }
  
  public String getGoalDeviationLabelStyle(TripStatusBean bean) {
    
    double minutes = bean.getGoalDeviation() / 60.0;

      if (minutes < -1.5) {
        return "tripStatusEarly";
      } else if (minutes < 1.5) {
        return "tripStatusDefault";
      } else {
        return "tripStatusDelayed";
      }
  }

  public String getGoalDeviationLabel(TripStatusBean bean) {
    int minutes = (int) Math.round(bean.getGoalDeviation() / 60.0);
    if( Math.abs(minutes) <= 1)
      return "On Time";
    String label = minutes >= 0 ? "Delay" : "Early";
    return Integer.toString(minutes) + " min " + label;
  }
  
  public String getEscapedRouteName(TripStatusBean bean) {
    String routeName = bean.getRouteName();
    while(routeName.length() < 3)
      routeName = "0" + routeName;
    return routeName;
  }
}
