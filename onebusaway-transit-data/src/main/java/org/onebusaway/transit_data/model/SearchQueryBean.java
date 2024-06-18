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

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;

import java.io.Serializable;

@QueryBean
public class SearchQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum EQueryType implements Serializable {
    BOUNDS, CLOSEST, BOUNDS_OR_CLOSEST, ORDERED_BY_CLOSEST
  }

  private EQueryType type;

  private CoordinateBounds bounds;

  private String query;

  private int maxCount;
  
  // default minimum lucene result score to keep
  private double minScoreToKeep = 1.0;

  private FilterChain systemFilterChain = new FilterChain();
  private FilterChain instanceFilterChain = new FilterChain();

  private AgencyServiceInterval serviceInterval;

  public EQueryType getType() {
    return type;
  }

  public void setType(EQueryType type) {
    this.type = type;
  }

  public CoordinateBounds getBounds() {
    return bounds;
  }

  public void setBounds(CoordinateBounds bounds) {
    this.bounds = bounds;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  public double getMinScoreToKeep() {
    return minScoreToKeep;
  }

  public void setMinScoreToKeep(double minScoreToKeep) {
    this.minScoreToKeep = minScoreToKeep;
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
}