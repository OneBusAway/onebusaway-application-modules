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
package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.RouteFilterBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookmarkPresentationServiceImpl implements
    BookmarkPresentationService {

  private TransitDataService _transitDataService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public List<BookmarkWithStopsBean> getBookmarksWithStops(
      List<BookmarkBean> bookmarks) {

    List<BookmarkWithStopsBean> beans = new ArrayList<BookmarkWithStopsBean>(
        bookmarks.size());

    for (BookmarkBean bookmark : bookmarks) {
      BookmarkWithStopsBean bean = new BookmarkWithStopsBean();
      bean.setId(bookmark.getId());
      bean.setName(bookmark.getName());
      bean.setStops(getStopsForStopIds(bookmark.getStopIds()));
      bean.setRoutes(getRoutesForRouteFilter(bookmark.getRouteFilter()));
      beans.add(bean);
    }

    return beans;
  }

  @Override
  public String getNameForStopIds(List<String> stopIds) {
    List<StopBean> stops = getStopsForStopIds(stopIds);
    return getNameForStops(stops);
  }

  @Override
  public String getNameForStops(List<StopBean> stops) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < stops.size(); i++) {
      if (i > 0) {
        if (i < stops.size() - 1)
          b.append(", ");
        else
          b.append(" and ");
      }
      StopBean stop = stops.get(i);
      b.append(stop.getName());
    }
    return b.toString();
  }

  @Override
  public String getNameForBookmark(BookmarkWithStopsBean bookmark) {
    String name = bookmark.getName();
    if (name != null)
      return name;
    return getNameForStops(bookmark.getStops());
  }

  /****
   * Private Methods
   ****/

  private List<StopBean> getStopsForStopIds(List<String> stopIds) {
    List<StopBean> stops = new ArrayList<StopBean>(stopIds.size());
    for (String stopId : stopIds) {
      StopBean stop = _transitDataService.getStop(stopId);
      if (stop != null)
        stops.add(stop);
    }
    return stops;
  }

  private List<RouteBean> getRoutesForRouteFilter(RouteFilterBean routeFilter) {
    List<RouteBean> routes = new ArrayList<RouteBean>();
    for (String routeId : routeFilter.getRouteIds()) {
      RouteBean route = _transitDataService.getRouteForId(routeId);
      if (route != null)
        routes.add(route);
    }
    Collections.sort(routes, new RouteNameComparator());
    return routes;
  }
}
