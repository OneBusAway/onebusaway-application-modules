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
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.util.SystemTime;

@QueryBean
public final class ArrivalsAndDeparturesQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time = SystemTime.currentTimeMillis();

  private int minutesBefore = 5;

  private int minutesAfter = 35;

  private int frequencyMinutesBefore = 2;

  private int frequencyMinutesAfter = 30;

  // should the queried for stopIds be included in nearby results
  private boolean includeInputIdsInNearby = false;

  private int maxCount = Integer.MAX_VALUE;

  // GTFS Route Type
  private List<Integer> routeTypes =  new ArrayList<>();

  private CoordinateBounds bounds;

  private HashSet<String> agenciesExcludingScheduled = new HashSet<>();

  public ArrivalsAndDeparturesQueryBean() {

  }

  public ArrivalsAndDeparturesQueryBean(ArrivalsAndDeparturesQueryBean bean) {
    this.time = bean.time;
    this.minutesBefore = bean.minutesBefore;
    this.minutesAfter = bean.minutesAfter;
    this.frequencyMinutesBefore = bean.frequencyMinutesBefore;
    this.frequencyMinutesAfter = bean.frequencyMinutesAfter;
    this.includeInputIdsInNearby = bean.includeInputIdsInNearby;
    this.bounds = bean.bounds;
    this.agenciesExcludingScheduled = bean.agenciesExcludingScheduled;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public int getMinutesBefore() {
    return minutesBefore;
  }

  public void setMinutesBefore(int minutesBefore) {
    this.minutesBefore = minutesBefore;
  }

  public int getMinutesAfter() {
    return minutesAfter;
  }

  public void setMinutesAfter(int minutesAfter) {
    this.minutesAfter = minutesAfter;
  }

  public int getFrequencyMinutesBefore() {
    return frequencyMinutesBefore;
  }

  public void setFrequencyMinutesBefore(int frequencyMinutesBefore) {
    this.frequencyMinutesBefore = frequencyMinutesBefore;
  }

  public int getFrequencyMinutesAfter() {
    return frequencyMinutesAfter;
  }

  public void setFrequencyMinutesAfter(int frequencyMinutesAfter) {
    this.frequencyMinutesAfter = frequencyMinutesAfter;
  }

  /**
   * if the queried for stopIds are included in the nearby results
   */
  public boolean getIncludeInputIdsInNearby() {
    return includeInputIdsInNearby;
  }

  /**
   * include the queried for stopIds in the nearby results
   * @param flag
   */
  public void setIncludeInputIdsInNearby(boolean flag) {
    this.includeInputIdsInNearby = flag;
  }

  public int getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  public CoordinateBounds getBounds() {
    return bounds;
  }

  public void setBounds(CoordinateBounds bounds) {
    this.bounds = bounds;
  }

  public List<Integer> getRouteTypes() {
    return routeTypes;
  }

  public void setRouteTypes(List<Integer> types) {
    this.routeTypes = types;
  }
  public void setRouteType(String routeType) {
    if (routeType == null) return;
    String[] types = routeType.split(",");
    for (String type : types) {
      try {
        routeTypes.add(Integer.parseInt(type));
      } catch (NumberFormatException nfe) {
        // bury
      }
    }
  }

  public void setAgenciesExcludingScheduled(HashSet<String> agencies){
    this.agenciesExcludingScheduled = agencies;
  }

  public HashSet<String> getAgenciesExcludingScheduled(){
    return this.agenciesExcludingScheduled;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + frequencyMinutesAfter;
    result = prime * result + frequencyMinutesBefore;
    result = prime * result + minutesAfter;
    result = prime * result + minutesBefore;
    result = prime * result + (int) (time ^ (time >>> 32));
    if (routeTypes != null)
      result = prime * result + routeTypes.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ArrivalsAndDeparturesQueryBean other = (ArrivalsAndDeparturesQueryBean) obj;
    if (frequencyMinutesAfter != other.frequencyMinutesAfter)
      return false;
    if (frequencyMinutesBefore != other.frequencyMinutesBefore)
      return false;
    if (minutesAfter != other.minutesAfter)
      return false;
    if (minutesBefore != other.minutesBefore)
      return false;
    if (time != other.time)
      return false;
    if (routeTypes == null || other.routeTypes == null)
      if (routeTypes != other.routeTypes)
        return false;
    if (!routeTypes.equals(other.routeTypes))
        return false;
    return true;
  }
}
