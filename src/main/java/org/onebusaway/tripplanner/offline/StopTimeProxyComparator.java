/**
 * 
 */
package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.StopTimeProxy;

import java.util.Comparator;

class StopTimeProxyComparator implements Comparator<StopTimeProxy> {

  private boolean _sortByArrivalTime;

  public StopTimeProxyComparator(boolean sortByArrivalTime) {
    _sortByArrivalTime = sortByArrivalTime;
  }

  public int compare(StopTimeProxy o1, StopTimeProxy o2) {
    int a = _sortByArrivalTime ? o1.getArrivalTime() : o1.getDepartureTime();
    int b = _sortByArrivalTime ? o2.getArrivalTime() : o2.getDepartureTime();
    return a == b ? 0 : (a < b ? -1 : 1);
  }
}