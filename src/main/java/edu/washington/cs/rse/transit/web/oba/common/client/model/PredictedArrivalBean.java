/*
 * Copyright 2008 Brian Ferris
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
package edu.washington.cs.rse.transit.web.oba.common.client.model;

import java.util.Date;

public class PredictedArrivalBean extends ApplicationBean implements
    Comparable<PredictedArrivalBean> {

  private static final long serialVersionUID = 1L;

  private String route;

  private String destination;

  private String _tripId;

  private long predictedTime;

  private long scheduledTime;

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getTripId() {
    return _tripId;
  }

  public void setTripId(String tripId) {
    _tripId = tripId;
  }

  public long getPredictedTime() {
    return predictedTime;
  }

  public void setPredictedTime(long predictedTime) {
    if (predictedTime < 1210000000000L)
      throw new IllegalStateException("BAD");
    this.predictedTime = predictedTime;
  }

  public long getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(long scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public boolean hasPredictedTime() {
    return this.predictedTime > 0;
  }

  public long getBestTime() {
    return hasPredictedTime() ? getPredictedTime() : getScheduledTime();
  }

  public Date getBestTimeAsDate() {
    return new Date(getBestTime());
  }

  public long getMaxTime() {
    long t = getScheduledTime();
    if (hasPredictedTime())
      t = Math.max(t, getPredictedTime());
    return t;
  }

  public int compareTo(PredictedArrivalBean o) {
    long a = getBestTime();
    long b = o.getBestTime();
    return a == b ? 0 : (a < b ? -1 : 1);
  }

  @Override
  public String toString() {
    return "route=" + route + " scheduled=" + scheduledTime + " predicted="
        + predictedTime;
  }

}
