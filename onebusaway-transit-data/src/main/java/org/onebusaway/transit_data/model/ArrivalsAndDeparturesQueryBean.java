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
import java.util.List;
import java.util.HashSet;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
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

  private CoordinateBounds bounds;

  private HashSet<String> agenciesExcludingScheduled = new HashSet<>();

  private FilterChain systemFilterChain = new FilterChain();
  private FilterChain instanceFilterChain = new FilterChain();

  private AgencyServiceInterval serviceInterval;

 private List<Integer> routeTypes;
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
    this.maxCount = bean.maxCount;
    this.systemFilterChain = bean.systemFilterChain;
    this.instanceFilterChain = bean.instanceFilterChain;
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


  public void setRouteTypes(List<Integer> types) {
      if (types == null || types.isEmpty()) return;
      instanceFilterChain.add(new ArrivalAndDepartureFilterByRouteType(types));
  }
  public void setRouteType(String routeType) {
    if (routeType == null) return;
    ArrivalAndDepartureFilterByRouteType arrivalAndDepartureFilterByRouteType = new ArrivalAndDepartureFilterByRouteType(routeType);
    routeTypes = arrivalAndDepartureFilterByRouteType.getRouteTypes();
    instanceFilterChain.add(arrivalAndDepartureFilterByRouteType);
  }

  public List<Integer> getRouteTypes(){
      return routeTypes;
  }

  public void setAgenciesExcludingScheduled(HashSet<String> agencies){
    this.agenciesExcludingScheduled = agencies;
  }

  public HashSet<String> getAgenciesExcludingScheduled(){
    return this.agenciesExcludingScheduled;
  }

  public FilterChain getSystemFilterChain() {
    return systemFilterChain;
  }

  public void setSystemFilterChain(FilterChain systemFilterChain) {
    this.systemFilterChain = systemFilterChain;
  }

  public FilterChain getInstanceFilterChain() {
    return instanceFilterChain;
  }

  public void setInstanceFilterChain(FilterChain instanceFilterChain) {
    this.instanceFilterChain = instanceFilterChain;
  }

  public AgencyServiceInterval getServiceInterval() {
    return serviceInterval;
  }

  public void setServiceInterval(AgencyServiceInterval serviceInterval) {
    this.serviceInterval = serviceInterval;
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
    if (instanceFilterChain != null)
      result = prime * result + instanceFilterChain.hashCode();
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
    if (instanceFilterChain == null || other.instanceFilterChain == null)
      if (instanceFilterChain != other.instanceFilterChain)
        return false;
    if (!instanceFilterChain.equals(other.instanceFilterChain))
      return false;
    return true;
  }
}
