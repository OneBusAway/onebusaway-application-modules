package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;

import java.util.Collection;

public interface TripAggregationStrategy {

  public void addTrip(TripPlan trip);

  public int getSize();

  public Collection<TripPlan> getTrips(Collection<TripPlan> results);
}
