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
package org.onebusaway.enterprise.webapp.actions.where;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;

@Results( {@Result(type = "redirectAction", name = "singleStopFound", params = {
    "actionName", "stop", "id", "${stop.id}", "parse", "true"})})
public class StopsAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  private String _code;

  private StopBean _stop;

  private List<StopBean> _stops;

  public void setCode(String code) {
    _code = code;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public StopBean getStop() {
    return _stop;
  }

  @Override
  @Actions( {
      @Action(value = "/where/iphone/stops")
  })
  public String execute() throws ServiceException {

    CoordinateBounds bounds = getServiceArea();

    if (bounds == null) {
      pushNextAction("stops", "code", _code);
      return "query-default-search-location";
    }
    
    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setBounds(bounds);
    searchQuery.setMaxCount(5);
    searchQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    searchQuery.setQuery(_code);
    
    StopsBean stopsResult = _transitDataService.getStops(searchQuery);

    _stops = stopsResult.getStops();

    if (_stops.size() == 0) {
      return "noStopsFound";
    } else if (_stops.size() > 1) {
      return "multipleStopsFound";
    } else {
      _stop = _stops.get(0);
      return "singleStopFound";
    }
  }
}
