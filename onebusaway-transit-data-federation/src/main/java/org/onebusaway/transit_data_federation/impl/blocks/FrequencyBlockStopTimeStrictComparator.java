package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.Comparator;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

class FrequencyBlockStopTimeStrictComparator implements
    Comparator<FrequencyBlockStopTimeEntry> {

  @Override
  public int compare(FrequencyBlockStopTimeEntry o1,
      FrequencyBlockStopTimeEntry o2) {

    FrequencyEntry f1 = o1.getFrequency();
    FrequencyEntry f2 = o2.getFrequency();

    if (f1.getStartTime() == f2.getStartTime()
        && f1.getEndTime() == f2.getEndTime()) {
      return 0;
    } else if (f1.getStartTime() < f2.getStartTime()
        && f1.getEndTime() < f2.getEndTime()) {
      return -1;
    } else {
      return 1;
    }
  }
}
