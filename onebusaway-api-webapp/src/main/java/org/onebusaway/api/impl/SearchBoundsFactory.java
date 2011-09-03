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
package org.onebusaway.api.impl;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;

public class SearchBoundsFactory {

  private double _lat;

  private double _lon;

  private double _radius;

  private double _latSpan;

  private double _lonSpan;

  private double _defaultSearchRadius;

  private double _maxSearchRadius;

  public SearchBoundsFactory() {

  }

  public SearchBoundsFactory(double maxSearchRadius) {
    _maxSearchRadius = maxSearchRadius;
  }

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  public void setRadius(double radius) {
    _radius = radius;
  }

  public void setLatSpan(double latSpan) {
    _latSpan = latSpan;
  }

  public void setLonSpan(double lonSpan) {
    _lonSpan = lonSpan;
  }

  public void setDefaultSearchRadius(double defaultSearchRadius) {
    _defaultSearchRadius = defaultSearchRadius;
  }

  public void setMaxSearchRadius(double maxSearchRadius) {
    _maxSearchRadius = maxSearchRadius;
  }

  public CoordinateBounds createBounds() {

    CoordinateBounds bounds = createInternalBounds();

    if (_maxSearchRadius > 0) {

      CoordinateBounds maxBounds = SphericalGeometryLibrary.bounds(_lat, _lon,
          _maxSearchRadius);

      double latSpan = (bounds.getMaxLat() - bounds.getMinLat());
      double lonSpan = (bounds.getMaxLon() - bounds.getMinLon());
      double maxLatSpan = (maxBounds.getMaxLat() - maxBounds.getMinLat());
      double maxLonSpan = (maxBounds.getMaxLon() - maxBounds.getMinLon());

      if (latSpan > maxLatSpan || lonSpan > maxLonSpan) {
        latSpan = Math.min(latSpan, maxLatSpan);
        lonSpan = Math.min(lonSpan, maxLonSpan);
        bounds = SphericalGeometryLibrary.boundsFromLatLonOffset(_lat, _lon,
            _latSpan / 2, _lonSpan / 2);
      }
    }

    return bounds;
  }

  private CoordinateBounds createInternalBounds() {
    if (_radius > 0)
      return SphericalGeometryLibrary.bounds(_lat, _lon, _radius);
    else if (_latSpan > 0 && _lonSpan > 0)
      return SphericalGeometryLibrary.boundsFromLatLonOffset(_lat, _lon,
          _latSpan / 2, _lonSpan / 2);
    else
      return SphericalGeometryLibrary.bounds(_lat, _lon, _defaultSearchRadius);
  }
}
