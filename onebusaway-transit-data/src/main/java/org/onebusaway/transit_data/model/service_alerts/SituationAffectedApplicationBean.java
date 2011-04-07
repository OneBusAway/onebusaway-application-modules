package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public final class SituationAffectedApplicationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}
