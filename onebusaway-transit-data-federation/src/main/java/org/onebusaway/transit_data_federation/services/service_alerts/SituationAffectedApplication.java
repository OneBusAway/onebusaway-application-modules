package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

public final class SituationAffectedApplication implements Serializable {

  private static final long serialVersionUID = 1L;

  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}
