package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

abstract class AbstractBlockStopTimeIndex<T extends HasBlockTrips> {

  protected final T _blockTripIndex;

  protected final int _stopIndex;

  private final ServiceInterval _serviceInterval;

  private final BlockStopTimeList _stopTimes = new BlockStopTimeList();

  public AbstractBlockStopTimeIndex(T blockTripIndex, int stopIndex,
      ServiceInterval serviceInterval) {
    if (blockTripIndex == null)
      throw new IllegalArgumentException("blockTripIndex is null");
    if (serviceInterval == null)
      throw new IllegalArgumentException("serviceInterval is null");
    _blockTripIndex = blockTripIndex;
    _stopIndex = stopIndex;
    _serviceInterval = serviceInterval;
  }

  public T getBlockIndex() {
    return _blockTripIndex;
  }

  public ServiceIdActivation getServiceIds() {
    return _blockTripIndex.getServiceIds();
  }

  public ServiceInterval getServiceInterval() {
    return _serviceInterval;
  }

  public List<BlockStopTimeEntry> getStopTimes() {
    return _stopTimes;
  }
  

  public int size() {
    return _stopTimes.size();
  }

  /*****
   * Private Methods
   ****/

  protected static ServiceInterval computeServiceInterval(HasBlockTrips blockIndex,
      int blockSequence) {

    ServiceInterval serviceInterval = null;

    List<BlockTripEntry> trips = blockIndex.getTrips();

    for (BlockTripEntry trip : trips) {

      BlockStopTimeEntry blockStopTime = trip.getStopTimes().get(blockSequence);
      StopTimeEntry stopTime = blockStopTime.getStopTime();

      serviceInterval = ServiceInterval.extend(serviceInterval,
          stopTime.getArrivalTime(), stopTime.getDepartureTime());
    }
    
    return serviceInterval;
  }

  private class BlockStopTimeList extends AbstractList<BlockStopTimeEntry> {

    @Override
    public int size() {
      return _blockTripIndex.getTrips().size();
    }

    @Override
    public BlockStopTimeEntry get(int index) {
      BlockTripEntry trip = _blockTripIndex.getTrips().get(index);
      return trip.getStopTimes().get(_stopIndex);
    }

  }
}
