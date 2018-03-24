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
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.onebusaway.nextbus.impl.rest.jackson.CapitalizeSerializer;
import org.onebusaway.nextbus.impl.rest.xstream.CapitalizeConverter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@JsonRootName("predictions")
@XStreamAlias("predictions")
public class Predictions {
  
  @XStreamAsAttribute
  @XStreamAlias("agencyTitle")
  @JsonProperty("agencyTitle")
  private String agencyTitle;
  
  @XStreamAsAttribute
  @XStreamAlias("routeTag")
  @JsonProperty("routeTag")
  private String routeShortName;
  
  private String routeId;
  
  @XStreamAsAttribute
  @XStreamAlias("routeTitle")
  @JsonProperty("routeTitle")
  private String routeName;
  
  @XStreamImplicit
  @JsonProperty("direction")
  private List<PredictionsDirection> dest;
  
  @XStreamAsAttribute
  @XStreamAlias("stopTitle")
  @XStreamConverter(CapitalizeConverter.class)
  @JsonProperty("stopTitle")
  @JsonSerialize(using = CapitalizeSerializer.class)
  private String stopName;
  
  @XStreamAsAttribute
  @XStreamAlias("stopTag")
  @JsonProperty("stopTag")
  private String stopId;

  public Predictions(){}
  
  public String getAgencyTitle() {
    return agencyTitle;
  }

  public void setAgencyTitle(String agencyTitle) {
    this.agencyTitle = agencyTitle;
  }

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

  public String getStopId() {
	return stopId;
  }

  public void setStopId(String stopId) {
	this.stopId = stopId;
  }
  
}

