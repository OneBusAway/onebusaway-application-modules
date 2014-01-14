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
package org.onebusaway.transit_data_federation.services.transit_graph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;

/**
 * Models a collection of active and inactive {@link LocalizedServiceId}. Useful
 * for modeling which service ids are active for a particular block of trips,
 * thereby determining the set of trips active for a particular block on a given
 * service date.
 * 
 * @author bdferris
 * @see BlockConfigurationEntry
 * @see ExtendedCalendarService
 */
public class ServiceIdActivation implements Serializable,
    Comparable<ServiceIdActivation> {

  private static final long serialVersionUID = 1L;

  private static ServiceIdListComparator _serviceIdListComparator = new ServiceIdListComparator();

  private final List<LocalizedServiceId> activeServiceIds;

  private final List<LocalizedServiceId> inactiveServiceIds;

  public ServiceIdActivation(LocalizedServiceId serviceId) {
    this(Arrays.asList(serviceId), empty());
  }

  public ServiceIdActivation(List<LocalizedServiceId> activeServiceIds,
      List<LocalizedServiceId> inactiveServiceIds) {

    if (activeServiceIds == null)
      throw new IllegalArgumentException("activeServiceIds is null");
    if (inactiveServiceIds == null)
      throw new IllegalArgumentException("inactiveServiceIds is null");
    if (activeServiceIds.isEmpty())
      throw new IllegalArgumentException("activeServiceIds is empty");

    this.activeServiceIds = activeServiceIds;
    this.inactiveServiceIds = inactiveServiceIds;
  }

  public List<LocalizedServiceId> getActiveServiceIds() {
    return activeServiceIds;
  }

  public List<LocalizedServiceId> getInactiveServiceIds() {
    return inactiveServiceIds;
  }

  public TimeZone getTimeZone() {
    return activeServiceIds.get(0).getTimeZone();
  }

  /****
   * {@link Comparable} Interface
   ****/

  @Override
  public int compareTo(ServiceIdActivation o) {
    int rc = _serviceIdListComparator.compare(this.activeServiceIds,
        o.activeServiceIds);
    if (rc != 0)
      return rc;
    return _serviceIdListComparator.compare(this.inactiveServiceIds,
        o.inactiveServiceIds);
  }

  /****
   * 
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + activeServiceIds.hashCode();
    result = prime * result + inactiveServiceIds.hashCode();
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
    ServiceIdActivation other = (ServiceIdActivation) obj;
    if (!activeServiceIds.equals(other.activeServiceIds))
      return false;
    if (!inactiveServiceIds.equals(other.inactiveServiceIds))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "active=" + activeServiceIds + " inactive=" + inactiveServiceIds;
  }

  private static final List<LocalizedServiceId> empty() {
    return Collections.emptyList();
  }
}