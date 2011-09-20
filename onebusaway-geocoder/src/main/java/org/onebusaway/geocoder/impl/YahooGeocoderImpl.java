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

import org.apache.commons.digester.Digester;

import java.net.MalformedURLException;
import java.net.URL;

public class YahooGeocoderImpl implements GeocoderService {

  private static final String BASE_URL = "http://local.yahooapis.com/MapsService/V1/geocode";

  private String _appId;

  public void setAppId(String appId) {
    _appId = appId;
  }

  public GeocoderResults geocode(String location) {

    StringBuilder b = new StringBuilder();
    b.append(BASE_URL);
    b.append("?");
    b.append("appid=").append(_appId);
    b.append("&location=").append(location);

    URL url = url(b.toString());

    Digester digester = createDigester();

    GeocoderResults results = new GeocoderResults();
    digester.push(results);

    try {
      digester.parse(url.openStream());
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }

    return results;
  }

  private Digester createDigester() {

    Digester digester = new Digester();

    Class<?>[] dType = {Double.class};

    digester.addObjectCreate("ResultSet/Result", GeocoderResult.class);
    digester.addCallMethod("ResultSet/Result/Latitude", "setLatitude", 0, dType);
    digester.addCallMethod("ResultSet/Result/Longitude", "setLongitude", 0,
        dType);
    digester.addCallMethod("ResultSet/Result/Address", "setAddress", 0);
    digester.addCallMethod("ResultSet/Result/City", "setCity", 0);
    digester.addCallMethod("ResultSet/Result/State", "setAdministrativeArea", 0);
    digester.addCallMethod("ResultSet/Result/Zip", "setPostalCode", 0);
    digester.addCallMethod("ResultSet/Result/Country", "setCountry", 0);
    digester.addSetNext("ResultSet/Result", "addResult");

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
