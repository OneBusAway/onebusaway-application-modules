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

public final class XYPoint implements Point, Serializable {

  private static final long serialVersionUID = 1L;

  private final double x;

  private final double y;

  public XYPoint(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public XYPoint(double[] ordinates) {
    this.x = ordinates[0];
    this.y = ordinates[1];
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  /****
   * {@link Point} Interface
   ****/

  @Override
  public int getDimensions() {
    return 2;
  }

  @Override
  public double getOrdinate(int index) {
    switch (index) {
      case 0:
        return x;
      case 1:
        return y;
      default:
        throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public XYPoint translate(double[] distances) {
    return new XYPoint(x + distances[0], y + distances[1]);
  }

  @Override
  public double getDistance(Point point) {
    XYPoint p = (XYPoint) point;
    return Math.sqrt(p2(x - p.x) + p2(y - p.y));
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "Point(" + x + "," + y + ")";
  }

  private static final double p2(double x) {
    return x *x;
  }
}
