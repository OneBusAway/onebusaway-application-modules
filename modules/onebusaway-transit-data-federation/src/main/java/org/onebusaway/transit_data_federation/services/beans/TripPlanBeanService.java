package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;

import java.util.Collection;
import java.util.List;

public interface TripPlanBeanService {

  public List<TripPlanBean> getTripsAsBeans(Collection<TripPlan> trips);

  public TripPlanBean getTripAsBean(TripPlan trip);
}
