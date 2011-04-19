package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.transit_graph.BlockTripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public abstract class AbstractBlockTripIndex implements HasBlockTrips {

  protected final List<BlockTripEntry> _trips;

  public AbstractBlockTripIndex(List<BlockTripEntry> trips) {
    if (trips == null)
      throw new IllegalArgumentException("trips is null");
    if (trips.isEmpty())
      throw new IllegalArgumentException("trips is empty");

    checkTripsHaveSameServiceids(trips);

    _trips = trips;
    for( BlockTripEntry trip : trips) {
      BlockTripEntryImpl tripImpl = (BlockTripEntryImpl) trip;
      tripImpl.setPattern(this);
    }
  }

  public List<BlockTripEntry> getTrips() {
    return _trips;
  }

  public ServiceIdActivation getServiceIds() {
    return _trips.get(0).getBlockConfiguration().getServiceIds();
  }
  
  public int size() {
    return _trips.size();
  }

  private static void checkTripsHaveSameServiceids(List<BlockTripEntry> trips) {
    ServiceIdActivation expected = trips.get(0).getBlockConfiguration().getServiceIds();
    for (int i = 1; i < trips.size(); i++) {
      ServiceIdActivation actual = trips.get(i).getBlockConfiguration().getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
