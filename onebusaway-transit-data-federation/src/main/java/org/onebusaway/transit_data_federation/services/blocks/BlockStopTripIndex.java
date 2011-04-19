package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class BlockStopTripIndex {

  private final BlockTripIndex _tripIndex;
  private final int _stopIndex;
  private final ServiceInterval _serviceInterval;

  public BlockStopTripIndex(BlockTripIndex tripIndex, int stopIndex) {
    _tripIndex = tripIndex;
    _stopIndex = stopIndex;
    _serviceInterval = computeServiceInterval(tripIndex, stopIndex);
  }
  
  public BlockTripIndex getTripIndex() {
    return _tripIndex;
  }

  public ServiceIdActivation getServiceIds() {
    return _tripIndex.getServiceIds();
  }

  public ServiceInterval getServiceInterval() {
    return _serviceInterval;
  }

  public int size() {
    return _tripIndex.size();
  }

  public BlockStopTimeEntry getBlockStopTimeForIndex(int index) {
    List<BlockTripEntry> trips = _tripIndex.getTrips();
    BlockTripEntry blockTripEntry = trips.get(index);
    return blockTripEntry.getStopTimes().get(_stopIndex);
  }

  public int getArrivalTimeForIndex(int index) {
    List<BlockTripEntry> trips = _tripIndex.getTrips();
    BlockTripEntry blockTripEntry = trips.get(index);
    return blockTripEntry.getArrivalTimeForIndex(_stopIndex);
  }

  public int getDepartureTimeForIndex(int index) {
    List<BlockTripEntry> trips = _tripIndex.getTrips();
    BlockTripEntry blockTripEntry = trips.get(index);
    return blockTripEntry.getDepartureTimeForIndex(_stopIndex);
  }

  private ServiceInterval computeServiceInterval(BlockTripIndex tripIndex,
      int stopIndex) {
    List<BlockTripEntry> trips = _tripIndex.getTrips();
    BlockStopTimeEntry fromBst = trips.get(0).getStopTimes().get(stopIndex);
    BlockStopTimeEntry toBst = trips.get(trips.size() - 1).getStopTimes().get(
        stopIndex);

    StopTimeEntry fromSt = fromBst.getStopTime();
    StopTimeEntry toSt = toBst.getStopTime();

    return new ServiceInterval(fromSt.getArrivalTime(),
        fromSt.getDepartureTime(), toSt.getArrivalTime(),
        toSt.getDepartureTime());
  }
}
