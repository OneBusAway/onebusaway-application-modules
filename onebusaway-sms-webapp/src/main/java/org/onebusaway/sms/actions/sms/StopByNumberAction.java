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

import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.springframework.beans.factory.annotation.Autowired;

public class StopByNumberAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private ServiceAreaService _serviceAreaService;

  private String _stopQuery;

  private List<StopBean> _stops;

  private int _selectedIndex = -1;

  private String _stopId;

  private String[] _args;

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  public String getStopQuery() {
    return _stopQuery;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public void setSelectedIndex(int selectedIndex) {
    _selectedIndex = selectedIndex;
  }

  public String getStopId() {
    return _stopId;
  }

  public String[] getArgs() {
    return _args;
  }

  @Override
  public String execute() throws ServiceException {

    if (_text != null)
      _text.trim();

    if (_text == null || _text.length() == 0)
      return INPUT;

    String[] tokens = _text.trim().split("\\s+");

    if (tokens.length == 0)
      return INPUT;

    CoordinateBounds serviceArea = _serviceAreaService.getServiceArea();

    if (serviceArea == null) {
      pushNextAction("stop-by-number", "text", _text);
      return "query-default-search-location";
    }

    _stopQuery = tokens[0];

    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setBounds(serviceArea);
    searchQuery.setMaxCount(5);
    searchQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    searchQuery.setQuery(_stopQuery);

    StopsBean results = _transitDataService.getStops(searchQuery);

    _stops = results.getStops();

    int stopIndex = 0;

    if (_stops.isEmpty()) {
      return "noStopsFound";
    } else if (_stops.size() > 1) {
      if (0 <= _selectedIndex && _selectedIndex < _stops.size()) {
        stopIndex = _selectedIndex;
      } else {
        pushNextAction("stop-by-number", "text", _text);
        pushNextAction("handle-multi-selection");
        return "multipleStopsFound";
      }
    }

    StopBean stop = _stops.get(stopIndex);
    _stopId = stop.getId();

    _args = new String[tokens.length - 1];
    System.arraycopy(tokens, 1, _args, 0, _args.length);

    return "arrivals-and-departures";
  }
}
