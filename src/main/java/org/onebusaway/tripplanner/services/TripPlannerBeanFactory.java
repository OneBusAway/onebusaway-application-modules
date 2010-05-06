package org.onebusaway.tripplanner.services;

import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.WalkSegmentBean;

import java.util.Collection;
import java.util.List;

public interface TripPlannerBeanFactory {

  public List<TripBean> getTripsAsBeans(Collection<TripPlan> trips);

  public TripBean getTripAsBean(TripPlan trip);

  public WalkSegmentBean getWalkPlanAsBean(long startTime, WalkPlan walk);
}
