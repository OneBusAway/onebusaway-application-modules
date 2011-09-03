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
package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

public class SituationConsequence implements Serializable {

  private static final long serialVersionUID = 1L;

  private TimeRange period;

  private String condition;

  private SituationConditionDetails conditionDetails;

  public TimeRange getPeriod() {
    return period;
  }

  public void setPeriod(TimeRange period) {
    this.period = period;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public SituationConditionDetails getConditionDetails() {
    return conditionDetails;
  }

  public void setConditionDetails(SituationConditionDetails conditionDetails) {
    this.conditionDetails = conditionDetails;
  }
}
