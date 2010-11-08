package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationExchangeDeliveryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationBean> situations;

  public List<SituationBean> getSituations() {
    return situations;
  }

  public void setSituations(List<SituationBean> situations) {
    this.situations = situations;
  }
}
