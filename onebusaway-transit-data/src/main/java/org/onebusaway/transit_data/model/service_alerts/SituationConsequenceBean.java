package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class SituationConsequenceBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private TimeRangeBean period;

  private String condition;

  private SituationConditionDetailsBean conditionDetails;

  public TimeRangeBean getPeriod() {
    return period;
  }

  public void setPeriod(TimeRangeBean period) {
    this.period = period;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public SituationConditionDetailsBean getConditionDetails() {
    return conditionDetails;
  }

  public void setConditionDetails(SituationConditionDetailsBean conditionDetails) {
    this.conditionDetails = conditionDetails;
  }

}
