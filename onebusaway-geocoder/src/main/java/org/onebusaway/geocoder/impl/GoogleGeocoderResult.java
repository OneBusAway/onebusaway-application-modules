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
package org.onebusaway.geocoder.impl;

import java.util.List;

import org.onebusaway.geocoder.model.GeocoderResult;

public class GoogleGeocoderResult extends GeocoderResult {

  private static final long serialVersionUID = 1L;

  public void addAddressComponent(GoogleAddressComponent addressComponent) {
    List<String> types = addressComponent.getTypes();
    if (types == null || types.size() == 0)
      return;

    // first type defines the address component
    String type = types.get(0);
    // we use the short name everywhere
    String shortName = addressComponent.getShortName();

    // use the super class's appropriate setter based on the type
    if (type.equals("street_number")) {
      setAddress(shortName);
    } else if (type.equals("locality")) {
      setCity(shortName);
    } else if (type.equals("postal_code")) {
      setPostalCode(shortName);
    } else if (type.equals("country")) {
      setCountry(shortName);
    } else if (type.equals("administrative_area_level_1")) {
      setAdministrativeArea(shortName);
    }
  }

  @Override
  public String getAddress() {
    String address = super.getAddress();
    return address != null ? address : "";
  }

  @Override
  public String toString() {
    return "Google Geocoder Result: " + getLatitude() + "," + getLongitude();
  }

}
