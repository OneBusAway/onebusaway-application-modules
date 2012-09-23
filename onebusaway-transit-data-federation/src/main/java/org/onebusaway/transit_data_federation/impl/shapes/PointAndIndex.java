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

import java.util.Comparator;

import org.onebusaway.geospatial.model.XYPoint;

public class PointAndIndex {
  
  public static final Comparator<PointAndIndex> DISTANCE_FROM_TARGET_COMPARATOR = new DistanceFromTargetComparator();

  public final XYPoint point;
  public final int index;
  public final double distanceFromTarget;
  public final double distanceAlongShape;

  public PointAndIndex(XYPoint point, int index, double distanceFromTarget,
      double distanceAlongShape) {
    this.point = point;
    this.index = index;
    this.distanceFromTarget = distanceFromTarget;
    this.distanceAlongShape = distanceAlongShape;
  }

  @Override
  public String toString() {
    return "xy=" + point.toString() + " index=" + index
        + " distanceFromTarget=" + distanceFromTarget + " distanceAlongShape="
        + distanceAlongShape;
  }

  public static class DistanceFromTargetComparator implements
      Comparator<PointAndIndex> {

    @Override
    public int compare(PointAndIndex a, PointAndIndex b) {
      if (a.distanceFromTarget == b.distanceFromTarget) {
        return 0;
      }
      return a.distanceFromTarget < b.distanceFromTarget ? -1 : 1;
    }
  }
}