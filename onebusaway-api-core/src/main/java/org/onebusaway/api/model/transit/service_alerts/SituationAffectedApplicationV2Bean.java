package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;

public class SituationAffectedApplicationV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}
