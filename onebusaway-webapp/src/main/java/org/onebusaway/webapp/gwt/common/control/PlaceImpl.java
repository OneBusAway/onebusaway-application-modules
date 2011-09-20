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
package org.onebusaway.webapp.gwt.common.control;

import java.util.Collections;
import java.util.List;

import com.google.gwt.maps.client.geom.LatLng;

public class PlaceImpl extends AbstractPlaceImpl {

  private String _name;
  private List<String> _description;
  private LatLng _location;
  private int _accuracy;

  @SuppressWarnings("unchecked")
  public PlaceImpl(String name, LatLng location, int accuracy) {
    this(name, Collections.EMPTY_LIST, location, accuracy);
  }

  public PlaceImpl(String name, List<String> description, LatLng location,
      int accuracy) {
    _name = name;
    _description = description;
    _location = location;
    _accuracy = accuracy;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public List<String> getDescription() {
    return _description;
  }

  @Override
  public LatLng getLocation() {
    return _location;
  }

  @Override
  public int getAccuracy() {
    return _accuracy;
  }

}
