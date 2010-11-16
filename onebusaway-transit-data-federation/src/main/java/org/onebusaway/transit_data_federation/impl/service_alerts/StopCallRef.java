package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class StopCallRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId _lineId;

  private final AgencyAndId _stopId;

  public StopCallRef(AgencyAndId lineId, AgencyAndId stopId) {
    _lineId = lineId;
    _stopId = stopId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_lineId == null) ? 0 : _lineId.hashCode());
    result = prime * result + ((_stopId == null) ? 0 : _stopId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StopCallRef other = (StopCallRef) obj;
    if (_lineId == null) {
      if (other._lineId != null)
        return false;
    } else if (!_lineId.equals(other._lineId))
      return false;
    if (_stopId == null) {
      if (other._stopId != null)
        return false;
    } else if (!_stopId.equals(other._stopId))
      return false;
    return true;
  }
}
