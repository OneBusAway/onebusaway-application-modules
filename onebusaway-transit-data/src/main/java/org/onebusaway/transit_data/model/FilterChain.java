/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

/**
 * List of filters to apply to API results.  These can be configured both
 * statically and dynamically.
 */
public class FilterChain implements Serializable {

  private List<ArrivalAndDepartureFilter> arrivalAndDepartureFilters = new ArrayList<>();

  public void setArrivalAndDepartureFilters(List<ArrivalAndDepartureFilter> arrivalAndDepartureFilters) {
    this.arrivalAndDepartureFilters = arrivalAndDepartureFilters;
  }

  private List<StopFilter> stopFilters = new ArrayList<>();

  public void setStopFilters(List<StopFilter> stopFilters) {
    this.stopFilters = stopFilters;
  }

  public void add(ArrivalAndDepartureFilter filter) {
    arrivalAndDepartureFilters.add(filter);
  }
  public void add(StopFilter filter) {
    stopFilters.add(filter);
  }

  public boolean matches(ArrivalAndDepartureBean bean) {
    if (arrivalAndDepartureFilters.isEmpty()) return true; // if empty we match everything
    boolean matches = true;
    for (ArrivalAndDepartureFilter filter : arrivalAndDepartureFilters) {
        matches = matches && filter.matches(bean);
    }
    return matches;
  }

  public boolean matches(StopBean bean) {
    if (stopFilters.isEmpty()) return true; // if empty we match everything
    for (StopFilter filter: stopFilters) {
      if (filter.matches(bean)) {
        return true; // short circuit on success
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    else if (!(obj instanceof FilterChain))
      return false;

    FilterChain fc = (FilterChain) obj;
    if (!this.arrivalAndDepartureFilters.equals(fc.arrivalAndDepartureFilters))
      return false;
    if (!this.stopFilters.equals(fc.stopFilters))
      return false;

    return true;
  }
  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    for (ArrivalAndDepartureFilter arrivalAndDepartureFilter : arrivalAndDepartureFilters) {
      result = prime * result + arrivalAndDepartureFilter.hashCode();
    }
    for (StopFilter stopFilter : stopFilters) {
      result = prime * result + stopFilter.hashCode();
    }
    return result;
  }

}
