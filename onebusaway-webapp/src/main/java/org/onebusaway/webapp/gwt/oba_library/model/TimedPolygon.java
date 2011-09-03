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
package org.onebusaway.webapp.gwt.oba_library.model;

import com.google.gwt.maps.client.overlay.Polygon;

public class TimedPolygon {

  private Polygon _polygon;

  private int _time;

  public TimedPolygon(Polygon polygon, int time) {
    _polygon = polygon;
    _time = time;
  }

  public Polygon getPolyline() {
    return _polygon;
  }

  public int getTime() {
    return _time;
  }
}
