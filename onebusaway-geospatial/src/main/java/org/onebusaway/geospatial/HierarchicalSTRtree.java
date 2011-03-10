package org.onebusaway.geospatial;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

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
