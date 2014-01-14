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
package org.onebusaway.webapp.gwt.oba_library.control;

public class Colormap {

  private double[][] _rgbs;

  private double[] _opacity;

  public Colormap(double[][] rgbs, double[] opacity) {
    _rgbs = rgbs;
    _opacity = opacity;
  }

  public String getColor(double ratio) {

    int index = (int) (ratio * _rgbs.length);
    if (index == _rgbs.length)
      index--;
    
    int r = (int) (_rgbs[index][0] * 255);
    int g = (int) (_rgbs[index][1] * 255);
    int b = (int) (_rgbs[index][2] * 255);

    return getColor(r, g, b);
  }

  public double getOpacity(double ratio) {
    int index = (int) (ratio * _opacity.length);
    if (index == _opacity.length)
      index--;
    
    return _opacity[index];
  }

  private String getColor(int r, int g, int b) {
    return "#" + hex(r) + hex(g) + hex(b);
  }

  private String hex(int value) {
    String s = Integer.toHexString(value);
    if (s.length() == 1)
      s = "0" + s;
    return s;
  }
}
