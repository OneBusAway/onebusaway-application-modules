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

import java.io.Serializable;

public class AgencyWithCoverageBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyBean agency;

  private double lat;

  private double lon;

  private double latSpan;
  
  private double lonSpan;
  
  public AgencyBean getAgency() {
    return agency;
  }

  public void setAgency(AgencyBean agency) {
    this.agency = agency;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getLatSpan() {
    return latSpan;
  }

  public void setLatSpan(double latSpan) {
    this.latSpan = latSpan;
  }

  public double getLonSpan() {
    return lonSpan;
  }

  public void setLonSpan(double lonSpan) {
    this.lonSpan = lonSpan;
  }
}
