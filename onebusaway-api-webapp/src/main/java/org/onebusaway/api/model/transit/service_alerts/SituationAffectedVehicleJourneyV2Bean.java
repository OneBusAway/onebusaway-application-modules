package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;

public final class SituationAffectedVehicleJourneyV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  /**
   * A Line in SIRI/TransModel speak is equivalent to a Route Collection in OBA
   */
  private String lineId;

  private String direction;

  public String getLineId() {
    return lineId;
  }

  public void setLineId(String lineId) {
    this.lineId = lineId;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }
}
