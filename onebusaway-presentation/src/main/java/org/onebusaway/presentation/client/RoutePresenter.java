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
package org.onebusaway.presentation.client;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;

public class RoutePresenter {
  
  public static String getNameForRoute(TripBean trip){
    String name = trip.getRouteShortName();
    if( name == null)
      name = getNameForRoute(trip.getRoute());
    return name;
  }
  
  public static String getNameForRoute(RouteBean route) {
    String name = route.getShortName();
    if (name == null)
      name = route.getLongName();
    if (name == null)
      name = route.getId();
    return name;
  }
  
  public static boolean isRouteNameLong(String name) {
    return name.length() > 5;
  }

  public static String getDescriptionForRoute(RouteBean route) {
    String value = route.getDescription();
    if (route.getShortName() != null && route.getLongName() != null)
      value = route.getLongName();
    return value;
  }
}
