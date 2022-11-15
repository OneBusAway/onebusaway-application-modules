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

import java.io.Serializable;

import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Trip narrative information. Includes information about the trip short name,
 * trip headsign, route shortname override, direction, and shape id.
 * 
 * @author bdferris
 * @see Trip
 * @see NarrativeService
 */
public final class TripNarrative implements Serializable {
 // 2nd version inculdes peakOffpeak
  private static final long serialVersionUID = 2L;

  private final String tripShortName;

  private final String tripHeadsign;

  private final String routeShortName;

  private final int peakOffpeak;

  public static Builder builder() {
    return new Builder();
  }

  private TripNarrative(Builder builder) {
    this.tripShortName = builder.tripShortName;
    this.tripHeadsign = builder.tripHeadsign;
    this.routeShortName = builder.routeShortName;
    this.peakOffpeak = builder.peakOffpeak;
  }

  public String getTripShortName() {
    return tripShortName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public int getPeakOffpeak() { return peakOffpeak; }

  public static class Builder {

    private String tripShortName;

    private String tripHeadsign;

    private String routeShortName;

    private int peakOffpeak;

    public TripNarrative create() {
      return new TripNarrative(this);
    }

    public Builder setTripShortName(String tripShortName) {
      this.tripShortName = tripShortName;
      return this;
    }

    public Builder setTripHeadsign(String tripHeadsign) {
      this.tripHeadsign = tripHeadsign;
      return this;
    }

    public Builder setRouteShortName(String routeShortName) {
      this.routeShortName = routeShortName;
      return this;
    }

    public Builder setPeakOffpeak(int peakOffpeak) {
      this.peakOffpeak = peakOffpeak;
      return this;
    }
  }
}
