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
package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.users.model.properties.Bookmark;

/**
 * 
 * @author bdferris
 * @see Bookmark
 */
public final class BookmarkBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String name;

  private List<String> stopIds;

  private RouteFilterBean routeFilter = new RouteFilterBean();

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  public RouteFilterBean getRouteFilter() {
    return routeFilter;
  }

  public void setRouteFilter(RouteFilterBean routeFilter) {
    this.routeFilter = routeFilter;
  }
}
