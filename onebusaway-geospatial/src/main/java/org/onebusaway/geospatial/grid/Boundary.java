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
package org.onebusaway.geospatial.grid;

import java.util.ArrayList;
import java.util.List;

public class Boundary {

  private BoundaryPath _outer;

  private List<BoundaryPath> _inner;

  public Boundary() {
    _inner = new ArrayList<BoundaryPath>();
  }

  public Boundary(BoundaryPath outer, List<BoundaryPath> inner) {
    _outer = outer;
    _inner = inner;
  }

  public BoundaryPath getOuterBoundary() {
    return _outer;
  }

  public void setOuterBoundary(BoundaryPath outer) {
    _outer = outer;
  }

  public List<BoundaryPath> getInnerBoundaries() {
    return _inner;
  }
  
  public void addInnerBoundary(BoundaryPath path) {
    _inner.add(path);
  }
}
