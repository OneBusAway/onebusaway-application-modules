/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.api.model.StopOnRoute;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import org.onebusaway.util.AgencyAndIdLibrary;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})
public class StopsWithinBoundsAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  private static Logger _log = LoggerFactory
      .getLogger(StopsWithinBoundsAction.class);
  
  @Autowired
  private TransitDataService _transitDataService;

  private List<StopOnRoute> _stops = new ArrayList<StopOnRoute>();

  private CoordinateBounds _bounds = null;
  
  public void setBounds(String bounds) {
    String[] coordinates = bounds.split(",");
    if(coordinates.length == 4) {
      _bounds = new CoordinateBounds(
          Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]),
          Double.parseDouble(coordinates[2]), Double.parseDouble(coordinates[3])
      );
    }
  }
  
  @Override
  public String execute() {    
    if(_bounds == null) {
      return SUCCESS;
    }
    
    SearchQueryBean queryBean = new SearchQueryBean();
    queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
    queryBean.setBounds(_bounds);
    queryBean.setMaxCount(200);    
    
    StopsBean stops = null;
    try {
      stops = _transitDataService.getStops(queryBean);
    } catch (OutOfServiceAreaServiceException e) {
      _log.error(" invalid results: ", e);
      return SUCCESS;
    }
    
    for(StopBean stop : stops.getStops()) {
      String agencyId = AgencyAndIdLibrary.convertFromString(stop.getId()).getAgencyId();
      if (_transitDataService.stopHasRevenueService(agencyId, stop.getId())) {
        _stops.add(new StopOnRoute(stop));
      }
    }
    
    return SUCCESS;
  }   

  /** 
   * VIEW METHODS
   */
  public List<StopOnRoute> getStops() {
    return _stops;
  }

}
