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
package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class LegBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private CoordinatePoint from;

  private CoordinatePoint to;

  private double distance;

  private String mode;

  private TransitLegBean transitLeg;

  private List<StreetLegBean> streetLegs;

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public CoordinatePoint getFrom() {
    return from;
  }

  public void setFrom(CoordinatePoint from) {
    this.from = from;
  }

  public CoordinatePoint getTo() {
    return to;
  }

  public void setTo(CoordinatePoint to) {
    this.to = to;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public TransitLegBean getTransitLeg() {
    return transitLeg;
  }

  public void setTransitLeg(TransitLegBean transitLeg) {
    this.transitLeg = transitLeg;
  }

  public List<StreetLegBean> getStreetLegs() {
    return streetLegs;
  }

  public void setStreetLegs(List<StreetLegBean> streetLegs) {
    this.streetLegs = streetLegs;
  }
}
