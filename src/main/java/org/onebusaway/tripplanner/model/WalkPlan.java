package org.onebusaway.tripplanner.model;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.impl.UtilityLibrary;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class WalkPlan implements Iterable<WalkNode>, Serializable {

  private static final long serialVersionUID = 1L;

  private final List<WalkNode> _path;

  private final double _distance;

  public WalkPlan(List<WalkNode> path) {
    _path = path;
    Point prev = null;
    double distance = 0.0;
    for (WalkNode node : path) {
      Point point = node.getLocation();
      if (prev != null)
        distance += UtilityLibrary.distance(prev, point);
      prev = point;
    }
    _distance = distance;
  }

  public List<WalkNode> getPath() {
    return _path;
  }

  public double getDistance() {
    return _distance;
  }

  public Iterator<WalkNode> iterator() {
    return _path.iterator();
  }
}
