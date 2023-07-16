/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

/**
 * Pattern Narrative information.
 */
public class RouteAndHeadsignNarrative implements Serializable {
  private String headsign;
  private String routeShortname;

  public RouteAndHeadsignNarrative() {

  }
  public RouteAndHeadsignNarrative(String headsign, String routeShortname) {
    this.headsign = headsign;
    this.routeShortname = routeShortname;
  }
  public String getHeadsign() {
    return headsign;
  }

  public void setHeadsign(String headsign) {
    this.headsign = headsign;
  }

  public String getRouteShortname() {
    return routeShortname;
  }

  public void setRouteShortname(String routeShortname) {
    this.routeShortname = routeShortname;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof RouteAndHeadsignNarrative))
      return false;
    RouteAndHeadsignNarrative rah = (RouteAndHeadsignNarrative) obj;
    if (rah.headsign == null && headsign != null)
      return false;
    if (rah.routeShortname == null && routeShortname != null)
      return false;
    return headsign.equals(rah.headsign)
            && routeShortname.equals(rah.routeShortname);
  }

  @Override
  public int hashCode() {
    int hash = 17;
    if (getHeadsign() != null)
      hash += getHeadsign().hashCode();
    if (getRouteShortname() != null)
      hash += getRouteShortname().hashCode();
    return hash;
  }
}
