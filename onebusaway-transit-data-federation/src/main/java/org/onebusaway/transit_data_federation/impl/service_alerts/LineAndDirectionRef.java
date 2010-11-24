package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class LineAndDirectionRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId _lineId;

  private final String _directionId;

  public LineAndDirectionRef(AgencyAndId lineId, String directionId) {
    _lineId = lineId;
    _directionId = directionId;
  }

  @Override
  public String toString() {
    return "(lineId=" + _lineId + ", directionId=" + _directionId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_directionId == null) ? 0 : _directionId.hashCode());
    result = prime * result + ((_lineId == null) ? 0 : _lineId.hashCode());
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
    LineAndDirectionRef other = (LineAndDirectionRef) obj;
    if (_directionId == null) {
      if (other._directionId != null)
        return false;
    } else if (!_directionId.equals(other._directionId))
      return false;
    if (_lineId == null) {
      if (other._lineId != null)
        return false;
    } else if (!_lineId.equals(other._lineId))
      return false;
    return true;
  }

}
