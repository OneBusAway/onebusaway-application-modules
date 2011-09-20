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

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

/**
 * Combines an {@link AgencyAndId} id with a service date. Typically, the id is
 * either a trip id or a block id. The service date is the "midnight" start-time
 * of a service date on which the trip is operating. Recall that "midnight" is
 * relative to the time-zone the trip or block is operating in (as opposed to the
 * one the server process is operating in) and to any trickiness with DST, as
 * defined in the GTFS spec.
 * 
 * @author bdferris
 */
public final class ServiceDateAndId implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long serviceDate;

  private final AgencyAndId id;

  public ServiceDateAndId(long serviceDate, AgencyAndId id) {
    this.id = id;
    this.serviceDate = serviceDate;
  }

  /**
   * @return the service date (Unix-time)
   */
  public long getServiceDate() {
    return serviceDate;
  }

  public AgencyAndId getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (int) (serviceDate ^ (serviceDate >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof ServiceDateAndId))
      return false;
    ServiceDateAndId other = (ServiceDateAndId) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (serviceDate != other.serviceDate)
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return id.toString() + " " + serviceDate;
  }
}
