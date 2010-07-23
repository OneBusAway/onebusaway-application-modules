package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

/**
 * Service methods for converting raw {@link WalkPlan} results into beans.
 * 
 * @author bdferris
 * @see WalkPlan
 * @see WalkSegmentBean
 */
public interface WalkPlanBeanService {

  /**
   * 
   * @param startTime the time when the walk starts (Unix-time)
   * @param duration how long the walk should take (ms)
   * @param walk the actual walk plan
   * @return a bean representation of the specified walk plan with a start time
   *         and duration
   */
  public WalkSegmentBean getWalkPlanAsBean(long startTime, long duration,
      WalkPlan walk);

  /**
   * 
   * @param startTime the time when the walk starts (Unix-time)
   * @param duration how long the walk should take (ms)
   * @param stopFrom the stop we're walking from
   * @param stopTo the stop we're walking to
   * @return construct a naive walk plan that draws a straight line from one
   *         stop to another
   */
  public WalkSegmentBean getStopsAsBean(long startTime, long duration,
      StopEntry stopFrom, StopEntry stopTo);
}
