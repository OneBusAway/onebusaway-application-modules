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
package org.onebusaway.nextbus.model.nextbus;

import java.math.BigDecimal;

import org.onebusaway.nextbus.impl.conversion.ListToStringConverter;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("vehicle")
@JsonRootName("vehicle")
public class Vehicle {
  
  @XStreamAsAttribute 
  private String id;
  
  @XStreamAsAttribute 
  private String routeTag;
  
  @XStreamAsAttribute 
  private String dirTag;
  
  @XStreamAsAttribute
  private BigDecimal lat;
  
  @XStreamAsAttribute
  private BigDecimal lon;
  
  @XStreamAsAttribute
  private int secsSinceReport;
  
  @XStreamAsAttribute
  private boolean predictable;
  
  @XStreamAsAttribute
  private int heading;
  
  @XStreamAsAttribute
  private Integer speedKmHr;
  
  @XStreamAsAttribute 
  private String tripTag;
  
  @XStreamAsAttribute 
  private String block;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRouteTag() {
    return routeTag;
  }

  public void setRouteTag(String routeTag) {
    this.routeTag = routeTag;
  }

  public String getDirTag() {
    return dirTag;
  }

  public void setDirTag(String dirTag) {
    this.dirTag = dirTag;
  }

  public BigDecimal getLat() {
    return lat;
  }

  public void setLat(BigDecimal lat) {
    this.lat = lat;
  }

  public BigDecimal getLon() {
    return lon;
  }

  public void setLon(BigDecimal lon) {
    this.lon = lon;
  }

  public int getSecsSinceReport() {
    return secsSinceReport;
  }

  public void setSecsSinceReport(int secsSinceReport) {
    this.secsSinceReport = secsSinceReport;
  }

  public boolean getPredictable() {
    return predictable;
  }

  public void setPredictable(boolean predictable) {
    this.predictable = predictable;
  }

  public int getHeading() {
    return heading;
  }

  public void setHeading(int d) {
    this.heading = d;
  }

  public Integer getSpeedKmHr() {
    return speedKmHr;
  }

  public String getTripTag() {
    return tripTag;
  }

  public void setTripTag(String tripTag) {
    this.tripTag = tripTag;
  }

  public String getBlock() {
    return block;
  }

  public void setBlock(String block) {
    this.block = block;
  }

  public void setSpeedKmHr(Integer speedKmHr) {
    this.speedKmHr = speedKmHr;
  }
}
