package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

abstract class AbstractBlockStopTimeIndex<T extends HasBlocks> {

  protected final T _blockIndex;

  private final int _blockSequence;

  private final ServiceInterval _serviceInterval;

  private final BlockStopTimeList _stopTimes = new BlockStopTimeList();

  public AbstractBlockStopTimeIndex(T blockIndex, int blockSequence,
      ServiceInterval serviceInterval) {
    if (blockIndex == null)
      throw new IllegalArgumentException("blockIndex is null");
    if (serviceInterval == null)
      throw new IllegalArgumentException("serviceInterval is null");
    _blockIndex = blockIndex;
    _blockSequence = blockSequence;
    _serviceInterval = serviceInterval;
  }

  public T getBlockIndex() {
    return _blockIndex;
  }

  public ServiceIdActivation getServiceIds() {
    return _blockIndex.getServiceIds();
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

  protected static ServiceInterval computeServiceInterval(HasBlocks blockIndex,
      int blockSequence) {

    ServiceInterval serviceInterval = null;

    List<BlockConfigurationEntry> blocks = blockIndex.getBlocks();

    for (BlockConfigurationEntry block : blocks) {

      BlockStopTimeEntry blockStopTime = block.getStopTimes().get(blockSequence);
      StopTimeEntry stopTime = blockStopTime.getStopTime();

      serviceInterval = ServiceInterval.extend(serviceInterval,
          stopTime.getArrivalTime(), stopTime.getDepartureTime());
    }
    return serviceInterval;
  }

  private class BlockStopTimeList extends AbstractList<BlockStopTimeEntry> {

    @Override
    public int size() {
      return _blockIndex.getBlocks().size();
    }

    @Override
    public BlockStopTimeEntry get(int index) {
      BlockConfigurationEntry block = _blockIndex.getBlocks().get(index);
      return block.getStopTimes().get(_blockSequence);
    }

  }
}
