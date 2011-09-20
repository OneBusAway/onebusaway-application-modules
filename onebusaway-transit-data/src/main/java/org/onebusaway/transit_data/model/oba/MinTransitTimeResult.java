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
package org.onebusaway.transit_data.model.oba;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolygonBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MinTransitTimeResult implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _complete = false;

  private List<EncodedPolygonBean> _polygons = new ArrayList<EncodedPolygonBean>();

  private List<Integer> _times = new ArrayList<Integer>();

  private List<CoordinateBounds> _searchGrid = new ArrayList<CoordinateBounds>();
  
  private MinTravelTimeToStopsBean _minTravelTimeToStops;

  public boolean isComplete() {
    return _complete;
  }

  public void setComplete(boolean complete) {
    _complete = complete;
  }

  public List<EncodedPolygonBean> getTimePolygons() {
    return _polygons;
  }

  /**
   * 
   * @return times, in minutes
   */
  public List<Integer> getTimes() {
    return _times;
  }

  public List<CoordinateBounds> getSearchGrid() {
    return _searchGrid;
  }

  public void setSearchGrid(List<CoordinateBounds> searchGrid) {
    _searchGrid = searchGrid;
  }
  
  public void setMinTravelTimeToStops(MinTravelTimeToStopsBean minTravelTimesToStops) {
    _minTravelTimeToStops = minTravelTimesToStops;
  }
  
  public MinTravelTimeToStopsBean getMinTravelTimeToStops() {
    return _minTravelTimeToStops;
  }
}
