/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
