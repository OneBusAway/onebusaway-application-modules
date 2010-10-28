package org.onebusaway.transit_data_federation.model.tripplanner;

import java.util.NoSuchElementException;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public final class EmptyStopEntriesWithValues implements StopEntriesWithValues {

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public StopEntry getStopEntry(int index) {
    throw new NoSuchElementException();
  }

  @Override
  public int getValue(int index) {
    throw new NoSuchElementException();
  }
}
