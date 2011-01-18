package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public abstract class AbstractBlockStopTimeIndex {

  protected final List<BlockConfigurationEntry> _blockConfigs;

  protected final int[] _stopIndices;

  private final ServiceInterval _serviceInterval;

  public AbstractBlockStopTimeIndex(List<BlockConfigurationEntry> blockConfigs,
      int[] stopIndices, ServiceInterval serviceInterval) {
    if (blockConfigs == null || blockConfigs.isEmpty())
      throw new IllegalArgumentException("trips is null or empty");
    if (stopIndices == null || stopIndices.length == 0)
      throw new IllegalArgumentException("stopIndices is null or empty");
    if (serviceInterval == null)
      throw new IllegalArgumentException("serviceInterval is null");
    _blockConfigs = blockConfigs;
    _stopIndices = stopIndices;
    _serviceInterval = serviceInterval;
  }

  public List<BlockConfigurationEntry> getBlockConfigs() {
    return _blockConfigs;
  }

  public ServiceInterval getServiceInterval() {
    return _serviceInterval;
  }

  public StopEntry getStop() {
    BlockStopTimeEntry blockStopTime = getStopTimeForIndex(0);
    return blockStopTime.getStopTime().getStop();
  }

  public ServiceIdActivation getServiceIds() {
    return _blockConfigs.get(0).getServiceIds();
  }

  public int size() {
    return _blockConfigs.size();
  }

  public List<BlockStopTimeEntry> getStopTimes() {
    return new BlockStopTimeList();
  }

  public List<BlockTripEntry> getTrips() {
    return new BlockTripList();
  }

  /****
   * 
   ****/

  protected BlockStopTimeEntry getStopTimeForIndex(int index) {
    BlockConfigurationEntry blockConfig = _blockConfigs.get(index);
    int stopIndex = _stopIndices[index];
    return blockConfig.getStopTimes().get(stopIndex);
  }

  /*****
   * Private Methods
   ****/

  protected static ServiceInterval computeServiceInterval(
      HasBlockTrips blockIndex, int blockSequence) {

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
      return _blockConfigs.size();
    }

    @Override
    public BlockStopTimeEntry get(int index) {
      return getStopTimeForIndex(index);
    }

  }

  private class BlockTripList extends AbstractList<BlockTripEntry> {

    @Override
    public int size() {
      return _blockConfigs.size();
    }

    @Override
    public BlockTripEntry get(int index) {
      BlockStopTimeEntry stopTime = getStopTimeForIndex(index);
      return stopTime.getTrip();
    }
  }
}
