/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.enterprise.webapp.actions.status.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "icinga_items")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class IcingaItem {

  @Id
  int id;
  
  @JsonProperty("SERVICE_NAME")
  @Column (name="SERVICE_NAME", length = 255)
  String serviceName;
  
  @JsonProperty("SERVICE_DISPLAY_NAME")
  @Column (name="SERVICE_DISPLAY_NAME", length = 255)
  String serviceDisplayName;
  
  @JsonProperty("SERVICE_CURRENT_STATE")
  @Column (name="SERVICE_CURRENT_STATE")
  int serviceCurrentState;
  
  @JsonProperty("SERVICE_OUTPUT")
  @Column (name="SERVICE_OUTPUT", length = 255)
  String serviceOutput;
  
  @JsonProperty("SERVICE_PERFDATA")
  @Column (name="SERVICE_PERFDATA", length = 255)
  String servicePerfdata;
  
  @JsonProperty("SERVICE_IS_PENDING")
  @Column (name="SERVICE_IS_PENDING")
  int serviceIsPending;
 
  public String getServiceName() {
    return serviceName;
  }
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }
  public String getServiceDisplayName() {
    return serviceDisplayName;
  }
  public void setServiceDisplayName(String serviceDisplayName) {
    this.serviceDisplayName = serviceDisplayName;
  }
  public int getServiceCurrentState() {
    return serviceCurrentState;
  }
  public void setServiceCurrentState(int serviceCurrentState) {
    this.serviceCurrentState = serviceCurrentState;
  }
  public String getServiceOutput() {
    return serviceOutput;
  }  
  public void setServiceOutput(String serviceOutput) {
    this.serviceOutput = serviceOutput;
  }
  public String getServicePerfdata() {
    return servicePerfdata;
  }
  public void setServicePerfdata(String servicePerfdata) {
    this.servicePerfdata = servicePerfdata;
  }
  public int getServiceIsPending() {
    return serviceIsPending;
  }
  public void setServiceIsPending(int serviceIsPending) {
    this.serviceIsPending = serviceIsPending;
  }
  
}
