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
package org.onebusaway.transit_data.model;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StopsForRouteBean implements Serializable {

  private static final long serialVersionUID = 3L;

  private RouteBean route;

  private List<StopBean> stops;

  private List<StopGroupingBean> stopGroupings;

  private List<EncodedPolylineBean> polylines;

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<StopGroupingBean> getStopGroupings() {
    return stopGroupings;
  }

  public void setStopGroupings(List<StopGroupingBean> groupings) {
    this.stopGroupings = groupings;
  }

  public List<EncodedPolylineBean> getPolylines() {
    return polylines;
  }

  public void setPolylines(List<EncodedPolylineBean> polylines) {
    this.polylines = polylines;
  }

  public void addGrouping(StopGroupingBean grouping) {
    if (stopGroupings == null)
      stopGroupings = new ArrayList<StopGroupingBean>();
    stopGroupings.add(grouping);
  }
}
