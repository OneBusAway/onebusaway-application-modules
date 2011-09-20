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
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class PointAndOrientation {

  private final CoordinatePoint point;

  private final double orientation;

  public PointAndOrientation(double lat, double lon, double orientation) {
    this.point = new CoordinatePoint(lat,lon);
    this.orientation = orientation;
  }

  public CoordinatePoint getPoint() {
    return point;
  }

  public double getOrientation() {
    return orientation;
  }
}
