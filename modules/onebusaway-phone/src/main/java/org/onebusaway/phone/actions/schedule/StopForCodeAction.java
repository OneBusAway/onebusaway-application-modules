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
package org.onebusaway.phone.actions.schedule;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;

import java.util.List;

public class StopForCodeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _stopCode;

  private String _stopId;

  public void setStopCode(String stopCode) {
    _stopCode = stopCode;
  }

  public String getStopId() {
    return _stopId;
  }

  public String execute() throws Exception {

    CoordinateBounds bounds = getDefaultSearchArea();
    if (bounds == null)
      return "needDefaultSearchLocation";

    StopsBean stopsBean = _transitDataService.getStopsByBoundsAndQuery(
        bounds.getMinLat(), bounds.getMinLon(), bounds.getMaxLat(),
        bounds.getMaxLon(), _stopCode, 9);

    List<StopBean> stops = stopsBean.getStops();

    if (stops.size() == 0) {
      return INPUT;
    } else if (stops.size() == 1) {
      StopBean stop = stops.get(0);
      _stopId = stop.getId();
      return SUCCESS;
    } else if (stops.size() > 1) {

    }

    return INPUT;
  }
}
