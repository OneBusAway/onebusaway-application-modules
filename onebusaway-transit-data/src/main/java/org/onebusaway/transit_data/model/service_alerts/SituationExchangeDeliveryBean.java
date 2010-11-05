package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationExchangeDeliveryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<AbstractSituationBean> situations;

  public List<AbstractSituationBean> getSituations() {
    return situations;
  }

  public void setSituations(List<AbstractSituationBean> situations) {
    this.situations = situations;
  }
}
