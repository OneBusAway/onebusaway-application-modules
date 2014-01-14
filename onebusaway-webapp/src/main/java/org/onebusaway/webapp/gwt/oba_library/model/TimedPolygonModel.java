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

import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.webapp.gwt.common.model.AbstractModel;

import com.google.gwt.maps.client.overlay.EncodedPolyline;
import com.google.gwt.maps.client.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class TimedPolygonModel extends AbstractModel<TimedPolygonModel> {

  private List<TimedPolygon> _polygons = new ArrayList<TimedPolygon>();

  private int _minTime;

  private int _maxTime;

  private boolean _complete = false;

  public List<TimedPolygon> getPolygons() {
    return _polygons;
  }

  public int getMinTime() {
    return _minTime;
  }

  public int getMaxTime() {
    return _maxTime;
  }

  public boolean isComplete() {
    return _complete;
  }

  public void setData(List<EncodedPolygonBean> polygons, List<Integer> times,
      boolean complete) {

    _polygons.clear();
    _minTime = Integer.MAX_VALUE;
    _maxTime = 0;
    _complete = complete;

    for (int i = 0; i < polygons.size(); i++) {

      EncodedPolygonBean bean = polygons.get(i);
      List<EncodedPolylineBean> inner = bean.getInnerRings();

      EncodedPolyline[] lines = new EncodedPolyline[1 + inner.size()];

      lines[0] = createPolyline(bean.getOuterRing());

      for (int x = 0; x < inner.size(); x++)
        lines[1 + x] = createPolyline(inner.get(x));

      int time = times.get(i);
      _minTime = Math.min(time, _minTime);
      _maxTime = Math.max(time, _maxTime);

      Polygon poly = Polygon.fromEncoded(lines, true, "#0000FF", 0.1, true);
      TimedPolygon tp = new TimedPolygon(poly, time);
      _polygons.add(tp);

    }

    refresh();
  }

  private EncodedPolyline createPolyline(EncodedPolylineBean outer) {
    EncodedPolyline ep = EncodedPolyline.newInstance();
    ep.setPoints(outer.getPoints());

    ep.setLevels(outer.getLevels(3));
    ep.setZoomFactor(32);
    ep.setNumLevels(4);
    return ep;
  }

  private void refresh() {
    fireModelChange(this);
  }
}
