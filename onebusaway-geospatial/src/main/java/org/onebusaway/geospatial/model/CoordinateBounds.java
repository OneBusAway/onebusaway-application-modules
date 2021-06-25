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

public final class CoordinateBounds implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _empty = true;

  private double _minLat;

  private double _minLon;

  private double _maxLat;

  private double _maxLon;

  public CoordinateBounds() {

  }

  public CoordinateBounds(double lat, double lon) {
    addPoint(lat, lon);
  }

  public CoordinateBounds(CoordinateBounds bounds) {
    addBounds(bounds);
  }

  public CoordinateBounds(double minLat, double minLon, double maxLat,
      double maxLon) {
    addPoint(minLat, minLon);
    addPoint(maxLat, maxLon);
  }

  public boolean isEmpty() {
    return _empty;
  }

  public void setEmpty(boolean empty) {
    _empty = empty;
  }

  public double getMinLat() {
    return _minLat;
  }

  public void setMinLat(double minLat) {
    _minLat = minLat;
  }

  public double getMinLon() {
    return _minLon;
  }

  public void setMinLon(double minLon) {
    _minLon = minLon;
  }

  public double getMaxLat() {
    return _maxLat;
  }

  public void setMaxLat(double maxLat) {
    _maxLat = maxLat;
  }

  public double getMaxLon() {
    return _maxLon;
  }

  public void setMaxLon(double maxLon) {
    _maxLon = maxLon;
  }

  public void addPoint(double lat, double lon) {
    if (_empty) {
      _empty = false;
      _minLat = lat;
      _minLon = lon;
      _maxLat = lat;
      _maxLon = lon;
    } else {
      _minLat = Math.min(_minLat, lat);
      _minLon = Math.min(_minLon, lon);
      _maxLat = Math.max(_maxLat, lat);
      _maxLon = Math.max(_maxLon, lon);
    }
  }

  public void addBounds(CoordinateBounds bounds) {
    addPoint(bounds.getMinLat(), bounds.getMinLon());
    addPoint(bounds.getMaxLat(), bounds.getMaxLon());
  }
  
  public void clear() {
    _empty = true;
  }

  public boolean contains(double lat, double lon) {
    return _minLat <= lat && lat <= _maxLat && _minLon <= lon && lon <= _maxLon;
  }

  public boolean contains(CoordinatePoint point) {
    return contains(point.getLat(), point.getLon());
  }

  public boolean intersects(CoordinateBounds r) {
    double minLat = Math.max(_minLat, r._minLat);
    double minLon = Math.max(_minLon, r._minLon);
    double maxLat = Math.min(_maxLat, r._maxLat);
    double maxLon = Math.min(_maxLon, r._maxLon);
    return minLat <= maxLat && minLon <= maxLon;
  }

  public CoordinateBounds intersection(CoordinateBounds r) {

    double minLat = Math.max(_minLat, r._minLat);
    double minLon = Math.max(_minLon, r._minLon);
    double maxLat = Math.min(_maxLat, r._maxLat);
    double maxLon = Math.min(_maxLon, r._maxLon);

    return new CoordinateBounds(minLat, minLon, maxLat, maxLon);
  }

  public CoordinateBounds union(CoordinateBounds r) {

    double minLat = Math.min(_minLat, r._minLat);
    double minLon = Math.min(_minLon, r._minLon);
    double maxLat = Math.max(_maxLat, r._maxLat);
    double maxLon = Math.max(_maxLon, r._maxLon);

    return new CoordinateBounds(minLat, minLon, maxLat, maxLon);
  }

  @Override
  public String toString() {
    return _minLat + "," + _minLon + "," + _maxLat + "," + _maxLon;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_empty ? 1231 : 1237);
    result = prime * result + Double.valueOf(_maxLat).hashCode();
    result = prime * result + Double.valueOf(_maxLon).hashCode();
    result = prime * result + Double.valueOf(_minLat).hashCode();
    result = prime * result + Double.valueOf(_minLon).hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof CoordinateBounds))
      return false;
    CoordinateBounds other = (CoordinateBounds) obj;
    if (_empty != other._empty)
      return false;
    if (_maxLat != other._maxLat)
      return false;
    if (_maxLon != other._maxLon)
      return false;
    if (_minLat != other._minLat)
      return false;
    if (_minLon != other._minLon)
      return false;
    return true;
  }

}
