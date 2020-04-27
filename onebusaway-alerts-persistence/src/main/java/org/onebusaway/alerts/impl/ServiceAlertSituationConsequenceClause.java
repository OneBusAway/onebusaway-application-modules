/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.alerts.impl;

import org.onebusaway.transit_data.model.service_alerts.EEffect;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "transit_data_service_alerts_situation_consequence")
public class ServiceAlertSituationConsequenceClause {

  @Id
  @GeneratedValue
  private int id = 0;

  @Enumerated(EnumType.STRING)
  private EEffect effect;

  private String detourPath;

  @ElementCollection(targetClass = String.class)
  private Set<String> detourStopIds = new HashSet<String>();

  @ManyToOne
  private ServiceAlertRecord serviceAlertRecord;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public EEffect getEffect() {
    return effect;
  }

  public void setEffect(EEffect effect) {
    this.effect = effect;
  }

  public String getDetourPath() {
    return detourPath;
  }

  public void setDetourPath(String detourPath) {
    this.detourPath = detourPath;
  }

  public Set<String> getDetourStopIds() {
    return detourStopIds;
  }

  public void setDetourStopIds(Set<String> detourStopIds) {
    this.detourStopIds = detourStopIds;
  }

  public ServiceAlertRecord getServiceAlertRecord() {
    return serviceAlertRecord;
  }

  public void setServiceAlertRecord(ServiceAlertRecord serviceAlertRecord) {
    this.serviceAlertRecord = serviceAlertRecord;
  }
}
