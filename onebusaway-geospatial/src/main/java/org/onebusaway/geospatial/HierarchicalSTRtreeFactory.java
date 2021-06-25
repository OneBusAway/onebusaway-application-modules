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
package org.onebusaway.geospatial;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;


public class HierarchicalSTRtreeFactory<T> {

  private Map<CoordinateBounds, STRtree> _treesByBounds = new HashMap<CoordinateBounds, STRtree>();

  private double _latStep = 0;

  private double _lonStep = 0;

  public void setLatStep(double latStep) {
    _latStep = latStep;
  }

  public void setLonStep(double lonStep) {
    _lonStep = lonStep;
  }

  public void setLatAndLonStep(double lat, double lon, double gridSize) {
    CoordinateBounds b = SphericalGeometryLibrary.bounds(lat, lon, gridSize / 2);
    _latStep = b.getMaxLat() - b.getMinLat();
    _lonStep = b.getMaxLon() - b.getMinLon();
  }

  public void add(double lat, double lon, T element) {

    STRtree tree = null;

    for (Map.Entry<CoordinateBounds, STRtree> entry : _treesByBounds.entrySet()) {
      CoordinateBounds bounds = entry.getKey();
      if (bounds.contains(lat, lon)) {
        tree = entry.getValue();
        break;
      }
    }

    if (tree == null) {

      double gLat = Math.floor(lat / _latStep) * _latStep;
      double gLon = Math.floor(lon / _lonStep) * _lonStep;

      CoordinateBounds b = new CoordinateBounds(gLat, gLon, gLat + _latStep,
          gLon + _lonStep);
      tree = new STRtree();
      _treesByBounds.put(b, tree);
    }

    Envelope env = new Envelope(lon, lon, lat, lat);
    tree.insert(env, element);
  }

  public HierarchicalSTRtree<T> create() {

    STRtree parentTree = new STRtree();

    for (Map.Entry<CoordinateBounds, STRtree> entry : _treesByBounds.entrySet()) {
      CoordinateBounds b = entry.getKey();
      Envelope env = new Envelope(b.getMinLon(), b.getMaxLon(), b.getMinLat(),
          b.getMaxLat());
      STRtree tree = entry.getValue();
      tree.build();
      parentTree.insert(env, tree);
    }
    parentTree.build();

    return new HierarchicalSTRtree<T>(parentTree);
  }
}
