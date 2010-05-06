package org.onebusaway.tripplanner;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.model.Walk;

public interface StopTransferWalkPlannerService {
  public Walk getWalkPlan(Stop from, Stop to) throws NoPathException;
}
