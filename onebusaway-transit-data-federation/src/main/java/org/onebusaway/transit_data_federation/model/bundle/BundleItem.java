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
package org.onebusaway.transit_data_federation.model.bundle;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.model.bundle.BundleFileItem;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BundleItem implements Serializable, Comparable<BundleItem>{

  private static final long serialVersionUID = 1L;

  private String id;
  
  private String name;
  
  private List<String> applicableAgencyIds;
  
  private ServiceDate serviceDateFrom;
  
  private ServiceDate serviceDateTo;

  private DateTime created;
  
  private DateTime updated;

  private ArrayList<BundleFileItem> files;
 
  public BundleItem() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getApplicableAgencyIds() {
    return applicableAgencyIds;
  }

  public void setApplicableAgencyIds(List<String> applicableAgencyIds) {
    this.applicableAgencyIds = applicableAgencyIds;
  }

  public ServiceDate getServiceDateFrom() {
    return serviceDateFrom;
  }

  public void setServiceDateFrom(ServiceDate serviceDateFrom) {
    this.serviceDateFrom = serviceDateFrom;
  }

  public ServiceDate getServiceDateTo() {
    return serviceDateTo;
  }

  public void setServiceDateTo(ServiceDate serviceDateTo) {
    this.serviceDateTo = serviceDateTo;
  }

  public DateTime getCreated() {
    return created;
  }

  public void setCreated(DateTime created) {
    this.created = created;
  }

  public DateTime getUpdated() {
    return updated;
  }

  public void setUpdated(DateTime updated) {
    this.updated = updated;
  }

  public ArrayList<BundleFileItem> getFiles() {
    return files;
  }

  public void setFiles(ArrayList<BundleFileItem> files) {
    this.files = files;
  }

  public boolean isApplicableToDate(ServiceDate date) {
    if(date.compareTo(serviceDateFrom) >= 0 && date.compareTo(serviceDateTo) <= 0) {
      return true;
    } else {
      return false;
    }
  }
  
  
  @Override
  public int compareTo(BundleItem otherBundle) {
    return this.getUpdated().compareTo(otherBundle.getUpdated());
  }
}