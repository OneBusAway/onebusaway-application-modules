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
package org.onebusaway.phone.actions.stops;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;

import com.opensymphony.xwork2.ModelDriven;

public class ArrivalsAndDeparturesForStopIdAction extends AbstractAction
    implements ModelDriven<StopsWithArrivalsAndDeparturesBean> {

  private static final long serialVersionUID = 1L;

  private List<String> _stopIds = new ArrayList<String>();

  private StopsWithArrivalsAndDeparturesBean _result;

  public void setStopIds(List<String> stopIds) {
    _stopIds.addAll(stopIds);
  }

  public StopsWithArrivalsAndDeparturesBean getModel() {
    return _result;
  }

  public void setModel(StopsWithArrivalsAndDeparturesBean result) {
    _result = result;
  }
  
  public String execute() throws Exception {
    
    if( _stopIds.isEmpty() )
      return INPUT;
    
    Calendar c = Calendar.getInstance();
    Date now = new Date();

    c.setTime(now);
    c.add(Calendar.MINUTE, -5);
    Date timeFrom = c.getTime();

    c.setTime(now);
    c.add(Calendar.MINUTE, 35);
    Date timeTo = c.getTime();

    _result = _transitDataService.getStopsWithArrivalsAndDepartures(_stopIds,
        timeFrom, timeTo);

    if (_result == null)
      return INPUT;
    
    _currentUserService.setLastSelectedStopIds(_stopIds);
    
    return SUCCESS;
  }
}
