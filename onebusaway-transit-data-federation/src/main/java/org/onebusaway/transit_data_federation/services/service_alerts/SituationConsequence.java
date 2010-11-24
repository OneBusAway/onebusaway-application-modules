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
