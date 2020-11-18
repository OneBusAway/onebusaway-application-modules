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
package org.onebusaway.alerts.impl;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "transit_data_service_alerts_time_ranges")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ServiceAlertTimeRange {

	@Id
	@GeneratedValue
  private final Integer id = 0;
  private Long fromValue;
  private Long toValue;

  @ManyToOne
  private ServiceAlertRecord serviceAlertRecord;

  public ServiceAlertRecord getServiceAlertRecord() {
    return serviceAlertRecord;
  }

  public void setServiceAlertRecord(ServiceAlertRecord serviceAlertRecord) {
    this.serviceAlertRecord = serviceAlertRecord;
  }

  public Integer getId() {
    return id;
  }

  public Long getFromValue() {
    return fromValue;
  }

  public void setFromValue(Long fromValue) {
    this.fromValue = fromValue;
  }

  public Long getToValue() {
    return toValue;
  }

  public void setToValue(Long toValue) {
    this.toValue = toValue;
  }
}
