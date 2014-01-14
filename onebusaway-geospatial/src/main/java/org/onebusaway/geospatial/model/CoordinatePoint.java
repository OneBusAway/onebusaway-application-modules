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
package org.onebusaway.geospatial.model;

import java.io.Serializable;

public final class CoordinatePoint implements Serializable {

  private static final long serialVersionUID = 1L;

  private double lat;

  private double lon;
  
  CoordinatePoint() {
    
  }

  public CoordinatePoint(double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  @Override
  public String toString() {
    return lat + " " + lon;
  }

  @Override
  public int hashCode() {
    return new Double(lat).hashCode() + new Double(lon).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o == null || !(o instanceof CoordinatePoint))
      return false;
    CoordinatePoint p = (CoordinatePoint) o;
    return this.lat == p.lat && this.lon == p.lon;
  }
}
