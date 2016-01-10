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
package org.onebusaway.admin.service.bundle.hastus.xml;

import java.util.ArrayList;
import java.util.List;

public class PttTrip {

  private String routeId;

  private String routePublicId;

  private int sequence;

  private List<PttTimingPoint> timingPoints = new ArrayList<PttTimingPoint>();

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRoutePublicId() {
    return routePublicId;
  }

  public void setRoutePublicId(String routePublicId) {
    this.routePublicId = routePublicId;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public List<PttTimingPoint> getTimingPoints() {
    return timingPoints;
  }

  public void setTimingPoints(List<PttTimingPoint> timingPoints) {
    this.timingPoints = timingPoints;
  }

  public void addTimingPoint(PttTimingPoint timingPoint) {
    timingPoints.add(timingPoint);
  }
}
