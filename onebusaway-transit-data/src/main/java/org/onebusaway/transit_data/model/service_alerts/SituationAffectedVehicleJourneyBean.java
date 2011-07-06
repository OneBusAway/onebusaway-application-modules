package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectedVehicleJourneyBean implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A Line in SIRI/TransModel speak is equivalent to a Route Collection in OBA
   */
  private String lineId;

  private String direction;

  private List<SituationAffectedCallBean> calls;

  private List<String> tripIds;

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

  public List<SituationAffectedCallBean> getCalls() {
    return calls;
  }

  public void setCalls(List<SituationAffectedCallBean> calls) {
    this.calls = calls;
  }

  public List<String> getTripIds() {
    return tripIds;
  }

  public void setTripIds(List<String> tripIds) {
    this.tripIds = tripIds;
  }
}
