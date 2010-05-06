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
