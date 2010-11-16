package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

public class SituationConsequence implements Serializable {

  private static final long serialVersionUID = 1L;

  private String condition;

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }
}
