package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;

public class SituationConsequenceV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private TimeRangeV2Bean period;

  private String condition;

  private SituationConditionDetailsV2Bean conditionDetails;

  public TimeRangeV2Bean getPeriod() {
    return period;
  }

  public void setPeriod(TimeRangeV2Bean period) {
    this.period = period;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public SituationConditionDetailsV2Bean getConditionDetails() {
    return conditionDetails;
  }

  public void setConditionDetails(
      SituationConditionDetailsV2Bean conditionDetails) {
    this.conditionDetails = conditionDetails;
  }
}
