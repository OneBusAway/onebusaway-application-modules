package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.services.tripplanner.ETripComparison;

public interface TripComparisonStrategy {
  public ETripComparison compare(TripStateStats statsA, TripStateStats statsB);
}
