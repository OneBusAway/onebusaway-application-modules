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
package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Bookmark implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private final int id;

  private final String name;

  private final List<String> stopIds;

  private final RouteFilter routeFilter;

  public Bookmark(int id, String name, List<String> stopIds, RouteFilter routeFilter) {
    this.id = id;
    this.name = name;
    this.stopIds = new ArrayList<String>(stopIds);
    this.routeFilter = routeFilter;
  }
  
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public RouteFilter getRouteFilter() {
    return routeFilter;
  }
}
