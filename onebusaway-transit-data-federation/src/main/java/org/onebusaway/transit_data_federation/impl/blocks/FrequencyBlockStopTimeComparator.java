package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.Comparator;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

class FrequencyBlockStopTimeComparator implements
    Comparator<FrequencyBlockStopTimeEntry> {

  @Override
  public int compare(FrequencyBlockStopTimeEntry o1,
      FrequencyBlockStopTimeEntry o2) {

    FrequencyEntry f1 = o1.getFrequency();
    FrequencyEntry f2 = o2.getFrequency();

    int c = f1.getStartTime() - f2.getStartTime();

    if (c != 0)
      return c;

    return f1.getEndTime() - f2.getEndTime();
  }
}
