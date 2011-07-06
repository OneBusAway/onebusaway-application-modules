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

  private String directionId;

  private List<SituationAffectedCall> calls;

  private List<AgencyAndId> tripIds;

  public AgencyAndId getLineId() {
    return lineId;
  }

  public void setLineId(AgencyAndId lineId) {
    this.lineId = lineId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public List<SituationAffectedCall> getCalls() {
    return calls;
  }

  public void setCalls(List<SituationAffectedCall> calls) {
    this.calls = calls;
  }

  public List<AgencyAndId> getTripIds() {
    return tripIds;
  }

  public void setTripIds(List<AgencyAndId> tripIds) {
    this.tripIds = tripIds;
  }
}
