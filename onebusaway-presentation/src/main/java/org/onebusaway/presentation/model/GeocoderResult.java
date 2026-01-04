/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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
package org.onebusaway.presentation.model;

import org.onebusaway.geospatial.model.CoordinateBounds;

/**
 * Result from geocoding a location query. This is a simplified local version
 * that replaces the dependency on onebusaway-geocoder module.
 */
public interface GeocoderResult {

  Double getLatitude();

  Double getLongitude();

  String getNeighborhood();

  String getFormattedAddress();

  CoordinateBounds getBounds();

  boolean isRegion();

}
