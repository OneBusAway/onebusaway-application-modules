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
package org.onebusaway.webapp.gwt.where_library;

import com.google.gwt.maps.client.geocode.GeoAddressAccuracy;

import java.util.HashMap;
import java.util.Map;

public class GeocoderAccuracyToBounds {
  private static Map<Integer, Integer> _accuracyToBounds = new HashMap<Integer, Integer>();

  static {

    _accuracyToBounds.put(9, 275);
    _accuracyToBounds.put(GeoAddressAccuracy.ADDRESS, 275);
    _accuracyToBounds.put(GeoAddressAccuracy.INTERSECTION, 275);
    _accuracyToBounds.put(GeoAddressAccuracy.STREET, 1800);
    _accuracyToBounds.put(GeoAddressAccuracy.POSTAL_CODE, 3600);
    _accuracyToBounds.put(GeoAddressAccuracy.TOWN, 14000);
    _accuracyToBounds.put(GeoAddressAccuracy.SUB_REGION, 30000);
  }

  public static int getBoundsInMetersByAccuracy(int accuracy) {
    Integer r = _accuracyToBounds.get(accuracy);
    if (r == null)
      r = 275;
    return r;
  }

  public static int getZoomLevelForAccuracy(int accuracy) {
    switch(accuracy) {
      case 10:
      case 9:
      case GeoAddressAccuracy.ADDRESS:
      case GeoAddressAccuracy.INTERSECTION:
        return 17;
      case GeoAddressAccuracy.STREET:
        return 15;
      case GeoAddressAccuracy.POSTAL_CODE:
        return 14;
      case GeoAddressAccuracy.TOWN:
        return 12;
      case GeoAddressAccuracy.SUB_REGION:
        return 11;
      case GeoAddressAccuracy.REGION:
        return 7;
      case GeoAddressAccuracy.COUNTRY:
        return 4;
      default:
        return 13;
    }
  }
}
