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
package org.onebusaway.api.model.transit.tripplanning;

import java.util.ArrayList;
import java.util.List;

public class ItinerariesV2Bean {

  private LocationV2Bean from = null;

  private LocationV2Bean to = null;

  private List<ItineraryV2Bean> itineraries = new ArrayList<ItineraryV2Bean>();

  public LocationV2Bean getFrom() {
    return from;
  }

  public void setFrom(LocationV2Bean from) {
    this.from = from;
  }

  public LocationV2Bean getTo() {
    return to;
  }

  public void setTo(LocationV2Bean to) {
    this.to = to;
  }

  public List<ItineraryV2Bean> getItineraries() {
    return itineraries;
  }

  public void setItineraries(List<ItineraryV2Bean> itineraries) {
    this.itineraries = itineraries;
  }
}
