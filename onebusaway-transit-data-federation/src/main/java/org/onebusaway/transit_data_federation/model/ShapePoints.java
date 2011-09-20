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
package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;

import java.io.Serializable;

/**
 * A more memory efficient data structure for capturing a sequence of
 * {@link ShapePoint} objects
 * 
 * @author bdferris
 * @see ShapePoint
 */
public class ShapePoints implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId shapeId;

  private double[] lats;

  private double[] lons;

  private double[] distTraveled;

  public int getSize() {
    return lats.length;
  }

  public boolean isEmpty() {
    return getSize() == 0;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  public double[] getLats() {
    return lats;
  }

  public void setLats(double[] lat) {
    this.lats = lat;
  }

  public double getLatForIndex(int index) {
    return lats[index];
  }

  public double[] getLons() {
    return lons;
  }

  public void setLons(double[] lon) {
    this.lons = lon;
  }

  public double getLonForIndex(int index) {
    return lons[index];
  }

  public double[] getDistTraveled() {
    return distTraveled;
  }

  public void setDistTraveled(double[] distTraveled) {
    this.distTraveled = distTraveled;
  }

  public double getDistTraveledForIndex(int index) {
    return distTraveled[index];
  }
  
  public CoordinatePoint getPointForIndex(int index) {
    return new CoordinatePoint(lats[index],lons[index]);
  }

  public void ensureDistTraveled() {

    if (distTraveled == null || distTraveled.length == 0)
      return;

    int n = distTraveled.length;

    if (distTraveled[n - 1] > 0)
      return;

    double totalDistanceTraveled = 0;
    double prevLat = lats[0];
    double prevLon = lons[0];

    for (int i = 1; i < n; i++) {
      double curLat = lats[i];
      double curLon = lons[i];
      totalDistanceTraveled += SphericalGeometryLibrary.distance(prevLat,
          prevLon, curLat, curLon);
      distTraveled[i] = totalDistanceTraveled;
      prevLat = curLat;
      prevLon = curLon;
    }
  }
}
