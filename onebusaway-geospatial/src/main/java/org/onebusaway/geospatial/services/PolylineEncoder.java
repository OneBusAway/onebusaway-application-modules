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
/**
 * Reimplementation of Mark McClures Javascript PolylineEncoder
 * 
 * All the mathematical logic is more or less copied by McClure
 * 
 * The gmaps java api is available at
 * http://sourceforge.net/project/showfiles.php?group_id=169331 Adapted by
 * Cassio Melo - cassio.ufpe[at]gmail[dot]com
 * 
 * @author Mark Rambow
 * @e-mail markrambow[at]gmail[dot]com
 * @version 0.1
 * 
 */
package org.onebusaway.geospatial.services;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class PolylineEncoder {

  public static EncodedPolylineBean createEncodings(double[] lat, double[] lon) {
    return createEncodings(new PointAdapterList(lat, lon));
  }

  public static EncodedPolylineBean createEncodings(double[] lat, double[] lon,
      int level) {
    return createEncodings(new PointAdapterList(lat, lon), level);
  }

  public static EncodedPolylineBean createEncodings(double[] lat, double[] lon,
      int offset, int length, int level) {
    return createEncodings(new PointAdapterList(lat, lon, offset, length),
        level);
  }

  public static EncodedPolylineBean createEncodings(
      Iterable<CoordinatePoint> points) {
    return createEncodings(points, -1);
  }

  /**
   * If level < 0, then {@link EncodedPolylineBean#getLevels()} will be null.
   * 
   * @param points
   * @param level
   * @return
   */
  public static EncodedPolylineBean createEncodings(
      Iterable<CoordinatePoint> points, int level) {

    StringBuilder encodedPoints = new StringBuilder();
    StringBuilder encodedLevels = new StringBuilder();

    int plat = 0;
    int plng = 0;
    int count = 0;

    for (CoordinatePoint trackpoint : points) {

      int late5 = floor1e5(trackpoint.getLat());
      int lnge5 = floor1e5(trackpoint.getLon());

      int dlat = late5 - plat;
      int dlng = lnge5 - plng;

      plat = late5;
      plng = lnge5;

      encodedPoints.append(encodeSignedNumber(dlat)).append(
          encodeSignedNumber(dlng));
      if (level >= 0)
        encodedLevels.append(encodeNumber(level));
      count++;
    }

    String pointsString = encodedPoints.toString();
    String levelsString = level >= 0 ? encodedLevels.toString() : null;
    return new EncodedPolylineBean(pointsString, levelsString, count);
  }

  public static List<CoordinatePoint> decode(EncodedPolylineBean polyline) {
      return decode(polyline.getPoints());
  }
  
  public static List<CoordinatePoint> decode(String pointString) {

    double lat = 0;
    double lon = 0;

    int strIndex = 0;
    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();

    while (strIndex < pointString.length()) {

      int[] rLat = decodeSignedNumberWithIndex(pointString, strIndex);
      lat = lat + rLat[0] * 1e-5;
      strIndex = rLat[1];

      int[] rLon = decodeSignedNumberWithIndex(pointString, strIndex);
      lon = lon + rLon[0] * 1e-5;
      strIndex = rLon[1];

      points.add(new CoordinatePoint(lat, lon));
    }

    return points;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private static final int floor1e5(double coordinate) {
    return (int) Math.floor(coordinate * 1e5);
  }

  public static String encodeSignedNumber(int num) {
    int sgn_num = num << 1;
    if (num < 0) {
      sgn_num = ~(sgn_num);
    }
    return (encodeNumber(sgn_num));
  }

  public static int decodeSignedNumber(String value) {
    int[] r = decodeSignedNumberWithIndex(value, 0);
    return r[0];
  }

  public static int[] decodeSignedNumberWithIndex(String value, int index) {
    int[] r = decodeNumberWithIndex(value, index);
    int sgn_num = r[0];
    if ((sgn_num & 0x01) > 0) {
      sgn_num = ~(sgn_num);
    }
    r[0] = sgn_num >> 1;
    return r;
  }

  public static String encodeNumber(int num) {

    StringBuffer encodeString = new StringBuffer();

    while (num >= 0x20) {
      int nextValue = (0x20 | (num & 0x1f)) + 63;
      encodeString.append((char) (nextValue));
      num >>= 5;
    }

    num += 63;
    encodeString.append((char) (num));

    return encodeString.toString();
  }

  public static int decodeNumber(String value) {
    int[] r = decodeNumberWithIndex(value, 0);
    return r[0];
  }

  public static int[] decodeNumberWithIndex(String value, int index) {

    if (value.length() == 0)
      throw new IllegalArgumentException("string is empty");

    int num = 0;
    int v = 0;
    int shift = 0;

    do {
      v = value.charAt(index++) - 63;
      num |= (v & 0x1f) << shift;
      shift += 5;
    } while (v >= 0x20);

    return new int[] {num, index};
  }

  private static class PointAdapterList extends AbstractList<CoordinatePoint> {

    private double[] _lat;
    private double[] _lon;
    private int _offset;
    private int _length;

    public PointAdapterList(double[] lat, double[] lon) {
      this(lat, lon, 0, lat.length);
    }

    public PointAdapterList(double[] lat, double[] lon, int offset, int length) {
      _lat = lat;
      _lon = lon;
      _offset = offset;
      _length = length;
    }

    @Override
    public CoordinatePoint get(int index) {
      return new CoordinatePoint(_lat[_offset + index], _lon[_offset + index]);
    }

    @Override
    public int size() {
      return _length;
    }
  }
}
