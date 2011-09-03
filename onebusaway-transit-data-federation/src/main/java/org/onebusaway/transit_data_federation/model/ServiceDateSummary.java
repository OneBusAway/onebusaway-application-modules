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
package org.onebusaway.transit_data_federation.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public final class ServiceDateSummary implements
    Comparable<ServiceDateSummary>, Serializable {

  private static final long serialVersionUID = 1L;

  private final Set<ServiceIdActivation> allServiceIds;

  private final List<ServiceDate> serviceDates;

  public ServiceDateSummary(Set<ServiceIdActivation> allServiceIds,
      List<ServiceDate> dates) {
    if (allServiceIds == null)
      throw new IllegalArgumentException("allServiceIds is null");
    if (dates == null)
      throw new IllegalArgumentException("dates is null");
    this.allServiceIds = allServiceIds;
    this.serviceDates = dates;
  }

  public Set<ServiceIdActivation> getAllServiceIds() {
    return allServiceIds;
  }

  public List<ServiceDate> getDates() {
    return serviceDates;
  }

  @Override
  public int compareTo(ServiceDateSummary o) {
    return this.serviceDates.size() - o.serviceDates.size();
  }
}
