package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

public final class SituationAffectedVehicleJourney implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A Line in SIRI/TransModel speak is equivalent to a Route Collection in OBA
   */
  private AgencyAndId lineId;

  private String direction;

  private List<SituationAffectedCall> calls;

  public AgencyAndId getLineId() {
    return lineId;
  }

  public void setLineId(AgencyAndId lineId) {
    this.lineId = lineId;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public List<SituationAffectedCall> getCalls() {
    return calls;
  }

  public void setCalls(List<SituationAffectedCall> calls) {
    this.calls = calls;
  }
}
