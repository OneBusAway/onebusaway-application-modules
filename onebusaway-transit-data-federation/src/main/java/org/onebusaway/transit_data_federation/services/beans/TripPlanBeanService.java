package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerService;

import java.util.Collection;
import java.util.List;

/**
 * Service for converting {@link TripPlan} low-level trip planner result objects
 * into high-level {@link TripPlanBean} representations.
 * 
 * @author bdferris
 * @see TripPlan
 * @see TripPlanBeanService
 * @see TripPlannerService
 * @see TripPlannerBeanService
 */
public interface TripPlanBeanService {

  /**
   * @param tripPlans trip plan results
   * @return a list of bean representations of the specified trip plans
   */
  public List<TripPlanBean> getTripsAsBeans(Collection<TripPlan> tripPlans);

  /**
   * @param tripPlan trip plan result
   * @return a bean representation of the specified trip plan
   */
  public TripPlanBean getTripAsBean(TripPlan tripPlan);
}
