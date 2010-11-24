package org.onebusaway.transit_data_federation.impl.service_alerts;

import org.onebusaway.gtfs.model.AgencyAndId;

public class LineDirectionAndStopCallRef {

  private final AgencyAndId _lineId;

  private final String _direction;

  private final AgencyAndId _stopId;

  public LineDirectionAndStopCallRef(AgencyAndId lineId, String direction,
      AgencyAndId stopId) {
    if (lineId == null)
      throw new IllegalStateException("lineId is null");
    if (direction == null)
      throw new IllegalStateException("direction is null");
    if (stopId == null)
      throw new IllegalStateException("stopId is null");
    _lineId = lineId;
    _direction = direction;
    _stopId = stopId;
  }

  @Override
  public String toString() {
    return "(lineId=" + _lineId + " direction=" + _direction + " stopId="
        + _stopId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _direction.hashCode();
    result = prime * result + _lineId.hashCode();
    result = prime * result + _stopId.hashCode();
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
    LineDirectionAndStopCallRef other = (LineDirectionAndStopCallRef) obj;
    if (!_direction.equals(other._direction))
      return false;
    if (!_lineId.equals(other._lineId))
      return false;
    if (!_stopId.equals(other._stopId))
      return false;
    return true;
  }

}
