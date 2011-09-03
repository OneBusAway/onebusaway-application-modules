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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.digester.Digester;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

public class GoogleGeocoderImpl implements GeocoderService {

  private static final String BASE_URL = "http://maps.google.com/maps/api/geocode/xml";

  private boolean sensor = false;

  public void setSensor(boolean sensor) {
    this.sensor = sensor;
  }

  public GeocoderResults geocode(String location) {
    StringBuilder b = new StringBuilder();
    b.append(BASE_URL);
    b.append("?");
    b.append("sensor=").append(sensor);
    String encodedLocation;
    try {
      encodedLocation = URLEncoder.encode(location, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new IllegalStateException("unknown encoding: UTF-8");
    }
    b.append("&address=").append(encodedLocation);

    URL url = url(b.toString());

    Digester digester = createDigester();

    GeocoderResults results = new GeocoderResults();
    digester.push(results);

    InputStream inputStream = null;
    try {
      inputStream = url.openStream();
      digester.parse(inputStream);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception ex) {
        }
      }
    }
    return results;
  }

  private Digester createDigester() {

    Digester digester = new Digester();

    Class<?>[] dType = {Double.class};

    digester.addObjectCreate("GeocodeResponse/result",
        GoogleGeocoderResult.class);

    digester.addObjectCreate("GeocodeResponse/result/address_component",
        GoogleAddressComponent.class);
    digester.addCallMethod(
        "GeocodeResponse/result/address_component/long_name", "setLongName", 0);
    digester.addCallMethod(
        "GeocodeResponse/result/address_component/short_name", "setShortName",
        0);
    digester.addCallMethod("GeocodeResponse/result/address_component/type",
        "addType", 0);
    digester.addSetNext("GeocodeResponse/result/address_component",
        "addAddressComponent");

    digester.addCallMethod("GeocodeResponse/result/geometry/location/lat",
        "setLatitude", 0, dType);
    digester.addCallMethod("GeocodeResponse/result/geometry/location/lng",
        "setLongitude", 0, dType);
    digester.addSetNext("GeocodeResponse/result", "addResult");

    return digester;
  }

  private URL url(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }
}
