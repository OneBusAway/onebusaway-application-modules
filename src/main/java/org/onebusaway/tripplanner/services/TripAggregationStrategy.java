package org.onebusaway.tripplanner.services;

import org.onebusaway.tripplanner.model.TripPlan;

import java.util.Collection;

public interface TripAggregationStrategy {

  public void addTrip(TripPlan trip);

  public int getSize();

  public Collection<TripPlan> getTrips(Collection<TripPlan> results);
}
