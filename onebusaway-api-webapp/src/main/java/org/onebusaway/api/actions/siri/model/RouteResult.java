/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri.model;

import java.util.List;

import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.RouteBean;

public class RouteResult implements SearchResult {

  private RouteBean route;

  private List<RouteDirection> directions;

  public RouteResult(RouteBean route, List<RouteDirection> directions) {
    this.route = route;
    this.directions = directions;
  }

  public String getId() {
    return route.getId();
  }

  public String getShortName() {
    return route.getShortName();
  }

  public String getLongName() {
    return route.getLongName();
  }

  public String getDescription() {
    return route.getDescription();
  }

  public String getColor() {
    if (route.getColor() != null) {
      return route.getColor();
    } else {
      return "000000";
    }
  }

  public List<RouteDirection> getDirections() {
    return directions;
  }
}
