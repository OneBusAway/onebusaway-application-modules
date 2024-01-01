/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter arrivals-and-departures results based on GTFS route_type.
 */
public class ArrivalAndDepartureFilterByRouteType extends ArrivalAndDepartureFilter {

  // GTFS Route Type
  private List<Integer> routeTypes =  new ArrayList<>();
  public ArrivalAndDepartureFilterByRouteType(List<Integer> types) {
    routeTypes.addAll(types);
  }

  public ArrivalAndDepartureFilterByRouteType(String routeTypeCommaDelimited) {
    String[] types = routeTypeCommaDelimited.split(",");
    for (String type : types) {
      try {
        routeTypes.add(Integer.parseInt(type));
      } catch (NumberFormatException nfe) {
        // bury
      }
    }

  }

  public List<Integer> getRouteTypes() {
    return routeTypes;
  }

  public boolean matches(ArrivalAndDepartureBean bean) {
    if (routeTypes == null  || routeTypes.isEmpty())
      return true; // no filter, everything matches

    for (Integer routeType : routeTypes) {
      if (bean.getStop() == null || bean.getStop().getRoutes() == null) continue;
      for (RouteBean route : bean.getStop().getRoutes()) {
        if (routeType == route.getType())
          return true;
      }
    }

    return false;

  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    else if (!(obj instanceof ArrivalAndDepartureFilterByRouteType))
      return false;

    ArrivalAndDepartureFilterByRouteType other = (ArrivalAndDepartureFilterByRouteType) obj;
    return routeTypes.equals(other.routeTypes);
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    for (Integer i :routeTypes) {
      result = prime * result + (i.hashCode());
    }
    return result;
  }
}
