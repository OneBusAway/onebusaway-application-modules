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

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlacemarkPlaceImpl extends AbstractPlaceImpl {

  private Placemark _placemark;

  public PlacemarkPlaceImpl(Placemark placemark) {
    _placemark = placemark;
  }

  public String getName() {
    return _placemark.getStreet();
  }

  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add(_placemark.getCity() + ", " + _placemark.getState() + " "
        + _placemark.getPostalCode());
    return desc;
  }

  public LatLng getLocation() {
    return _placemark.getPoint();
  }

  public int getAccuracy() {
    return _placemark.getAccuracy();
  }

  @Override
  public String toString() {
    return "Place(name=" + getName() + " description="
        + getDescriptionAsString() + " accuracy=" + _placemark.getAccuracy()
        + ")";
  }

}
