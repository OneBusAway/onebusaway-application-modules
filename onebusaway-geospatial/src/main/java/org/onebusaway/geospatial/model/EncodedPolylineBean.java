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
 * 
 */
package org.onebusaway.geospatial.model;

import java.io.Serializable;

public class EncodedPolylineBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String points;

  private String levels;

  private int length;

  public EncodedPolylineBean() {

  }

  public EncodedPolylineBean(String points, String levels, int length) {
    this.points = points;
    this.levels = levels;
    this.length = length;
  }

  public String getPoints() {
    return points;
  }

  public void setPoints(String points) {
    this.points = points;
  }

  public String getLevels() {
    return levels;
  }

  public String getLevels(int defaultLevel) {
    if (levels == null) {
      StringBuilder b = new StringBuilder();
      String l = encodeNumber(defaultLevel);
      for (int i = 0; i < length; i++)
        b.append(l);
      return b.toString();
    }
    return levels;
  }

  public void setLevels(String levels) {
    this.levels = levels;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  private static String encodeNumber(int num) {

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
}