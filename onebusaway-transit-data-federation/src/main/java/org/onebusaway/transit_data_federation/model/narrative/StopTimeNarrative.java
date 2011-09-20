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
package org.onebusaway.transit_data_federation.model.narrative;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

import java.io.Serializable;

/**
 * Stop time narrative information. Includes information about the stop headsign
 * and route shortname.
 * 
 * @author bdferris
 * @see StopTime
 * @see NarrativeService
 */
public final class StopTimeNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String stopHeadsign;

  private final String routeShortName;

  public static Builder builder() {
    return new Builder();
  }

  private StopTimeNarrative(Builder builder) {
    this.stopHeadsign = builder.stopHeadsign;
    this.routeShortName = builder.routeShortName;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((routeShortName == null) ? 0 : routeShortName.hashCode());
    result = prime * result
        + ((stopHeadsign == null) ? 0 : stopHeadsign.hashCode());
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
    StopTimeNarrative other = (StopTimeNarrative) obj;
    if (routeShortName == null) {
      if (other.routeShortName != null)
        return false;
    } else if (!routeShortName.equals(other.routeShortName))
      return false;
    if (stopHeadsign == null) {
      if (other.stopHeadsign != null)
        return false;
    } else if (!stopHeadsign.equals(other.stopHeadsign))
      return false;
    return true;
  }

  public static class Builder {

    private String stopHeadsign;

    private String routeShortName;

    public StopTimeNarrative create() {
      return new StopTimeNarrative(this);
    }

    public Builder setStopHeadsign(String stopHeadsign) {
      this.stopHeadsign = stopHeadsign;
      return this;
    }

    public Builder setRouteShortName(String routeShortName) {
      this.routeShortName = routeShortName;
      return this;
    }
  }
}
