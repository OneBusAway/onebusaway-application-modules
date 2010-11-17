package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;
import java.util.List;

public class SituationsContainer implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Situation> situations;

  public List<Situation> getSituations() {
    return situations;
  }

  public void setSituations(List<Situation> situations) {
    this.situations = situations;
  }
}
