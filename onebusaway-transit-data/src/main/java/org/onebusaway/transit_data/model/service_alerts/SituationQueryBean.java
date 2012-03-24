/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.QueryBean;

/**
 * A situation query has the following semantics. The semantics of specifying
 * multiple ids within an {@link AffectsBean} is an AND relationship. As an
 * example, specifying routeId + directionId indicates a match against alerts
 * affecting the specified route and direction. By the same token, specifying
 * tripId + stopId would indicate a match against alerts affecting the specified
 * trip and stop. The semantics of specifying multiple AffectsBeans in the query
 * is an OR relationship. To find an alert affecting any stop in a collection of
 * stops, you'd create an {@link AffectsBean} for each stop with stopId set and
 * add them all to the query.
 * 
 * @author bdferris
 * 
 */
@QueryBean
public class SituationQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  // Asked Brian about time in developer list, he said:
  // "Generally, I'd set it to now(). You can use it to query what service
  // ids were active at some point in the past or future by changing the
  // time value."
  // http://groups.google.com/group/onebusaway-developers/msg/fb9b44cd9ba7bef4?hl=en
  private long time;

  private List<AffectsBean> affects = new ArrayList<AffectsBean>();

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public List<AffectsBean> getAffects() {
    return affects;
  }

  public void setAffects(List<AffectsBean> affects) {
    this.affects = affects;
  }

  public static class AffectsBean {

    private String agencyId;

    private String routeId;

    private String directionId;

    private String tripId;

    private String stopId;

    public String getAgencyId() {
      return agencyId;
    }

    public void setAgencyId(String agencyId) {
      this.agencyId = agencyId;
    }

    public String getRouteId() {
      return routeId;
    }

    public void setRouteId(String routeId) {
      this.routeId = routeId;
    }

    public String getDirectionId() {
      return directionId;
    }

    public void setDirectionId(String directionId) {
      this.directionId = directionId;
    }

    public String getTripId() {
      return tripId;
    }

    public void setTripId(String tripId) {
      this.tripId = tripId;
    }

    public String getStopId() {
      return stopId;
    }

    public void setStopId(String stopId) {
      this.stopId = stopId;
    }
  }
}
