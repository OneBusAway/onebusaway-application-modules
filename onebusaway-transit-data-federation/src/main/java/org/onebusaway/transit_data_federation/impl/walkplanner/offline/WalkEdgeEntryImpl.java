/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import org.onebusaway.transit_data_federation.services.walkplanner.WalkEdgeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

public class WalkEdgeEntryImpl implements WalkEdgeEntry {

  private final WalkNodeEntry _from;

  private final WalkNodeEntry _to;

  private double _distance;

  public WalkEdgeEntryImpl(WalkNodeEntry from, WalkNodeEntry to,
      double distance) {
    _from = from;
    _to = to;
    _distance = distance;
  }

  public double getDistance() {
    return _distance;
  }

  public WalkNodeEntry getNodeFrom() {
    return _from;
  }

  public WalkNodeEntry getNodeTo() {
    return _to;
  }

  @Override
  public boolean equals(Object obj) {
    if( obj == null || !(obj instanceof WalkEdgeEntryImpl))
      return false;
    WalkEdgeEntryImpl edge = (WalkEdgeEntryImpl) obj;
    return _from.equals(edge._from) && _to.equals(edge._to);
  }

  @Override
  public int hashCode() {
    return _from.hashCode() + 31 * _to.hashCode();
  }
}