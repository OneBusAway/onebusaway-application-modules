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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.onebusaway.geospatial.model.CoordinateBounds;

public class HierarchicalSTRtree<T> {

  private STRtree _parentTree;

  HierarchicalSTRtree(STRtree parentTree) {
    _parentTree = parentTree;
  }

  @SuppressWarnings("unchecked")
  public List<T> query(CoordinateBounds b) {

    List<T> results = new ArrayList<T>();
    Envelope env = new Envelope(b.getMinLon(), b.getMaxLon(), b.getMinLat(),
        b.getMaxLat());

    List<STRtree> subTrees = _parentTree.query(env);
    for (STRtree subTree : subTrees) {
      List<T> result = subTree.query(env);
      results.addAll(result);
    }

    return results;
  }
}
