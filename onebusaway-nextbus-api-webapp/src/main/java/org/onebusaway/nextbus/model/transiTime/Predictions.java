/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.model.transiTime;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.nextbus.model.nextbus.ScheduleTableRow;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("predictions")
public class Predictions {
  
  @XStreamAsAttribute
  @XStreamAlias("routeTag")
  private String routeId;
  
  @XStreamAsAttribute
  @XStreamAlias("routeCode")
  private String routeShortName;
  
  @XStreamAsAttribute
  @XStreamAlias("routeTitle")
  private String routeName;
  
  @XStreamAsAttribute
  @XStreamAlias("stopTitle")
  private String stopName;
  
  @XStreamImplicit
  private List<PredictionsDirection> dest;
  
  public Predictions(){}
  
  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getStopName() {
    return stopName;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;
  }

  public String getRouteShortName(){
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public List<PredictionsDirection> getDest() {
    return dest;
  }

  public void setDest(List<PredictionsDirection> dest) {
    this.dest = dest;
  }


}

