package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.onebusaway.common.impl.UtilityLibrary;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class Walk implements Iterable<WalkNode>, Serializable {

  private static final long serialVersionUID = 1L;

  private final List<WalkNode> _path;

  private final double _distance;

  public Walk(WalkNode from, WalkNode to) {
    _path = CollectionsLibrary.getValuesAsList(from, to);
    _distance = UtilityLibrary.distance(from.getLocation(), to.getLocation());
  }

  public Walk(List<WalkNode> path, double distance) {
    _path = path;
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
