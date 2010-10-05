package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;

public class ServiceIdListComparator implements
    Comparator<List<LocalizedServiceId>> {

  @Override
  public int compare(List<LocalizedServiceId> o1, List<LocalizedServiceId> o2) {

    // Recall that we want longer lists to come first
    int rc = o2.size() - o1.size();

    if (rc != 0)
      return rc;

    for (int i = 0; i < o1.size(); i++) {
      LocalizedServiceId lsid1 = o1.get(i);
      LocalizedServiceId lsid2 = o2.get(i);
      rc = lsid1.compareTo(lsid2);
      if (rc != 0)
        return rc;
    }

    return 0;
  }

}
