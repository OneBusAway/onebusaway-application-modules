package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectedVehicleJourneyV2Bean implements
    Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A Line in SIRI/TransModel speak is equivalent to a Route Collection in OBA
   */
  private String lineId;

  private String directionId;

  private List<SituationAffectedCallV2Bean> calls;

  private List<String> tripIds;

  public String getLineId() {
    return lineId;
  }

  public void setLineId(String lineId) {
    this.lineId = lineId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public List<SituationAffectedCallV2Bean> getCalls() {
    return calls;
  }

  public void setCalls(List<SituationAffectedCallV2Bean> calls) {
    this.calls = calls;
  }

  public List<String> getTripIds() {
    return tripIds;
  }

  public void setTripIds(List<String> tripIds) {
    this.tripIds = tripIds;
  }
}
