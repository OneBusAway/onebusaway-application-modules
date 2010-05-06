/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

import java.util.Comparator;

class StopTimeProxyComparator implements Comparator<StopTimeEntry> {

  private boolean _sortByArrivalTime;

  public StopTimeProxyComparator(boolean sortByArrivalTime) {
    _sortByArrivalTime = sortByArrivalTime;
  }

  public int compare(StopTimeEntry o1, StopTimeEntry o2) {
    int a = _sortByArrivalTime ? o1.getArrivalTime() : o1.getDepartureTime();
    int b = _sortByArrivalTime ? o2.getArrivalTime() : o2.getDepartureTime();
    return a == b ? 0 : (a < b ? -1 : 1);
  }
}