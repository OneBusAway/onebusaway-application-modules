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
package org.onebusaway.gtfs.model;

import java.io.Serializable;

public class AgencyAndId implements Serializable, Comparable<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String id;

  public AgencyAndId() {

  }

  public AgencyAndId(String agencyId, String id) {
    this.agencyId = agencyId;
    this.id = id;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean hasValues() {
    return this.agencyId != null && this.id != null;
  }

  public int compareTo(AgencyAndId o) {
    int c = this.agencyId.compareTo(o.agencyId);
    if (c == 0)
      c = this.id.compareTo(o.id);
    return c;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + agencyId.hashCode();
    result = prime * result + id.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AgencyAndId))
      return false;
    AgencyAndId other = (AgencyAndId) obj;
    if (!agencyId.equals(other.agencyId))
      return false;
    if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return agencyId + " " + id;
  }
}
