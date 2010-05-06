package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripStateStats;
import org.onebusaway.tripplanner.services.ETripComparison;

public interface TripComparisonStrategy {
  public ETripComparison compare(TripStateStats statsA, TripStateStats statsB);
}
