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

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

public class FixedGeocoderImpl implements GeocoderService {

  private double _lat;

  private double _lon;

  private String _address;

  private String _city;

  private String _state;

  private String _postalCode;

  private String _country;

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  public void setAddress(String address) {
    _address = address;
  }

  public void setCity(String city) {
    _city = city;
  }

  public void setState(String state) {
    _state = state;
  }

  public void setPostalCode(String postalCode) {
    _postalCode = postalCode;
  }

  public void setCountry(String country) {
    _country = country;
  }

  @Override
  public GeocoderResults geocode(String location) {

    GeocoderResult result = new GeocoderResult();
    result.setLatitude(_lat);
    result.setLongitude(_lon);
    result.setAddress(_address);
    result.setCity(_city);
    result.setAdministrativeArea(_state);
    result.setPostalCode(_postalCode);
    result.setCountry(_country);

    GeocoderResults results = new GeocoderResults();
    results.addResult(result);
    return results;
  }

}
