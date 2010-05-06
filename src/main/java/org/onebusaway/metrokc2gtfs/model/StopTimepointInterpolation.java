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
package org.onebusaway.metrokc2gtfs.model;

public class StopTimepointInterpolation implements Comparable<StopTimepointInterpolation> {

  private static final long serialVersionUID = 1L;

  private int id;

  private ServicePatternKey servicePattern;

  private int stop;

  private int stopIndex;

  private TimepointAndIndex timepointFrom;

  private TimepointAndIndex timepointTo;

  private double ratio;

  private double totalDistance;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ServicePatternKey getServicePattern() {
    return servicePattern;
  }

  public void setServicePattern(ServicePatternKey servicePattern) {
    this.servicePattern = servicePattern;
  }

  public int getStop() {
    return stop;
  }

  public void setStop(int stop) {
    this.stop = stop;
  }

  public void setStopIndex(int index) {
    this.stopIndex = index;
  }

  public int getStopIndex() {
    return this.stopIndex;
  }

  public TimepointAndIndex getTimepointFrom() {
    return this.timepointFrom;
  }

  public void setTimepointFrom(TimepointAndIndex timepointFrom) {
    this.timepointFrom = timepointFrom;
  }

  public TimepointAndIndex getTimepointTo() {
    return this.timepointTo;
  }

  public void setTimepointTo(TimepointAndIndex timepointTo) {
    this.timepointTo = timepointTo;
  }

  public double getRatio() {
    return ratio;
  }

  public void setRatio(double ratio) {
    this.ratio = ratio;
  }

  public double getTotalDistanceTraveled() {
    return this.totalDistance;
  }

  public void setTotalDistanceTraveled(double totalDistance) {
    this.totalDistance = totalDistance;
  }

  public int compareTo(StopTimepointInterpolation o) {
    return this.stopIndex == o.stopIndex ? 0 : (this.stopIndex < o.stopIndex ? -1 : 1);
  }

  @Override
  public String toString() {
    return "STI(id=" + id + " servicePattern=" + servicePattern + " stop=" + stop + " stopIndex=" + stopIndex
        + " timepointFrom=" + timepointFrom + " timepointTo=" + timepointTo + " ratio=" + ratio + ")";
  }
}
