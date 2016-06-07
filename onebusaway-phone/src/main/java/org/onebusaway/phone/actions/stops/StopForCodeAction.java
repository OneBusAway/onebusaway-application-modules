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

import java.util.Arrays;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;

public class StopForCodeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _stopCode;

  private List<String> _stopIds;

  private List<StopBean> _stops;

  public void setStopCode(String stopCode) {
    _stopCode = stopCode;
  }

  public List<String> getStopIds() {
    return _stopIds;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public String execute() throws Exception {

    CoordinateBounds bounds = getDefaultSearchArea();
    if (bounds == null)
      return NEEDS_DEFAULT_SEARCH_LOCATION;

    if (_stopCode == null || _stopCode.length() == 0)
      return INPUT;

    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setBounds(bounds);
    searchQuery.setMaxCount(5);
    searchQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    searchQuery.setQuery(_stopCode);

    StopsBean stopsBean = _transitDataService.getStops(searchQuery);

    _stops = stopsBean.getStops();

    if (_stops.size() == 0) {
      return "noStopsFound";
    } else if (_stops.size() == 1) {
      StopBean stop = _stops.get(0);
      _stopIds = Arrays.asList(stop.getId());
      return SUCCESS;
    } else {
      return "multipleStopsFound";
    }
  }
}
