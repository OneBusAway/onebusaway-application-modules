/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model;

import org.onebusaway.gtfs.model.calendar.ServiceDate;

import java.io.Serializable;

/**
 * Represents the start and end dates of GTFS.
 * 
 */
public class ServiceDateRange implements Serializable {

  private static final long serialVersionUID = 5043315153284104718L;
  private ServiceDate _startDate;
  private ServiceDate _endDate;
  private String _agencyId;
  public ServiceDateRange() {

  }

  public ServiceDateRange(String agencyId, ServiceDate startDate, ServiceDate endDate) {
    _agencyId = agencyId;
    _startDate = startDate;
    _endDate = endDate;
  }

  public ServiceDate getStartDate() {
    return _startDate;
  }

  public void setStartDate(ServiceDate startDate) {
    _startDate = startDate;
  }

  public ServiceDate getEndDate() {
    return _endDate;
  }

  public void setEndDate(ServiceDate endDate) {
    _endDate = endDate;
  }

  public String getAgencyId() {
    return _agencyId;
  }
  
  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ServiceDateRange))
      return false;
    ServiceDateRange sdr = (ServiceDateRange) obj;
    if (sdr.getStartDate() == null && getStartDate() != null)
      return false;
    if (sdr.getEndDate() == null && getEndDate() != null)
      return false;
    return (sdr.getStartDate().equals(getStartDate()) && sdr.getEndDate().equals(
        getEndDate()));
  }

  @Override
  public int hashCode() {
    int hash = 17;
    if (getStartDate() != null)
      hash += getStartDate().hashCode();
    if (getEndDate() != null)
      hash += getEndDate().hashCode();
    return hash;
  }

}
