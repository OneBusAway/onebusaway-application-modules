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

import java.io.Serializable;

public class MinTravelTimeToStopsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String[] stopIds;

  private double[] lats;

  private double[] lons;

  private long[] transitTimes;

  private double walkingVelocity;

  public MinTravelTimeToStopsBean() {

  }

  public MinTravelTimeToStopsBean(String agencyId, String[] stopIds,
      double[] lats, double[] lons, long[] times, double walkingVelocity) {
    this.agencyId = agencyId;
    this.stopIds = stopIds;
    this.lats = lats;
    this.lons = lons;
    this.transitTimes = times;
    this.walkingVelocity = walkingVelocity;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public String[] getStopIds() {
    return stopIds;
  }

  public double[] getLats() {
    return lats;
  }

  public double[] getLons() {
    return lons;
  }

  public long[] getTransitTimes() {
    return transitTimes;
  }

  public double getWalkingVelocity() {
    return walkingVelocity;
  }

  public int getSize() {
    return stopIds.length;
  }

  public String getStopId(int index) {
    return stopIds[index];
  }

  public double getStopLat(int i) {
    return lats[i];
  }

  public double getStopLon(int i) {
    return lons[i];
  }

  public long getTravelTime(int i) {
    return transitTimes[i];
  }

}
