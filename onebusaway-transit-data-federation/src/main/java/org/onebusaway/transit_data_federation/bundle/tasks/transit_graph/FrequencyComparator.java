package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.Comparator;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyComparator implements Comparator<FrequencyEntry> {

  @Override
  public int compare(FrequencyEntry entryA, FrequencyEntry entryB) {

    int rc = entryA.getStartTime() - entryB.getStartTime();

    if (rc != 0)
      return rc;

    rc = entryA.getEndTime() - entryB.getEndTime();

    if (rc != 0)
      return rc;

    return entryA.getHeadwaySecs() - entryB.getHeadwaySecs();
  }
}
