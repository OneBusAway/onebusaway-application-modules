package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

public interface WalkPlanBeanService {
  public WalkSegmentBean getWalkPlanAsBean(long startTime, long duration, WalkPlan walk);
  public WalkSegmentBean getStopsAsBean(long startTime, long duration, StopEntry stopFrom, StopEntry stopTo);
}
