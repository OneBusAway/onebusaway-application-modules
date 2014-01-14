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
package org.onebusaway.geospatial.services;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class UTMLibrary {

  private static final String LAT_ZONES = "CDEFGHJKLMNPQRSTUVWX";
  
  public static UTMProjection getProjectionForPoint(CoordinatePoint point) {
    return new UTMProjection(getUTMZoneForLongitude(point.getLon()));
  }
  
  public static UTMProjection getProjectionForPoint(double lat, double lon) {
    return new UTMProjection(getUTMZoneForLongitude(lon));
  }

  public static String getUTMZone(CoordinatePoint point) {
    return getUTMZone(point.getLat(), point.getLon());
  }

  public static String getUTMZone(double lat, double lon) {
    return Integer.toString(getUTMZoneForLongitude(lon))
        + getUTMZoneForLatitude(lat);
  }

  public static int getUTMZoneForLongitude(double lon) {

    if (lon < -180 || lon > 180)
      throw new IllegalArgumentException(
          "Coordinates not within UTM zone limits");

    int lonZone = (int) ((lon + 180) / 6);

    if (lonZone == 60)
      lonZone--;
    return lonZone + 1;
  }

  public static char getUTMZoneForLatitude(double lat) {
    int latZone = getUTMIndexForLatitude(lat);
    return LAT_ZONES.charAt(latZone);
  }

  private static int getUTMIndexForLatitude(double lat) {
    if (lat < -80 || lat > 84)
      throw new IllegalArgumentException(
          "Coordinates not within UTM zone limits");

    int latZone = (int) ((lat + 80) / 8);

    if (latZone == 20)
      latZone--;
    return latZone;
  }
}
