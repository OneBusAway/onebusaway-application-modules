/*
 * Copyright 2008 Greg Briggs
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
package org.onebusaway.where.web.common.client.model;

import org.onebusaway.common.web.common.client.model.ApplicationBean;
import org.onebusaway.common.web.common.client.model.StopBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopsWithArrivalsBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private List<StopBean> _stops;

  private List<DepartureBean> _arrivals;
  

  public StopsWithArrivalsBean() {

  }

  public StopsWithArrivalsBean(List<StopBean> stops, List<DepartureBean> arrivals) {
    _stops = stops;
    _arrivals = arrivals;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public List<DepartureBean> getPredictedArrivals() {
    return _arrivals;
  }
  
  public Set<StopBean> getNearbyStops() {
    Set<StopBean> nearby = new HashSet<StopBean>();
    for (StopBean stop : _stops) {
      nearby.addAll(stop.getNearbyStops());
    }
    //If we are viewing it, then don't list it, we have enough stops already.
    nearby.removeAll(_stops); 
    return nearby;
  }
}
