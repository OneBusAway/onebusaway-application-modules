package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class BlockStopSequenceIndex implements HasIndexedBlockStopTimes {

  private final BlockSequenceIndex _index;
  private final int _offset;
  private final ServiceInterval _serviceInterval;

  private final List<BlockStopTimeEntry> _stopTimes = new ListImpl();

  public BlockStopSequenceIndex(BlockSequenceIndex index, int offset) {
    _index = index;
    _offset = offset;
    _serviceInterval = computeServiceInterval(index, offset);
  }

  public BlockSequenceIndex getIndex() {
    return _index;
  }

  public int getOffset() {
    return _offset;
  }

  public ServiceIdActivation getServiceIds() {
    return _index.getServiceIds();
  }

  public ServiceInterval getServiceInterval() {
    return _serviceInterval;
  }

  public int size() {
    return _index.size();
  }

  public BlockStopTimeEntry getBlockStopTimeForIndex(int index) {
    return _stopTimes.get(index);
  }

  /****
   * {@link HasIndexedBlockStopTimes} Interface
   ****/

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    return _stopTimes;
  }

  public int getArrivalTimeForIndex(int index) {
    List<BlockSequence> sequences = _index.getSequences();
    BlockSequence sequence = sequences.get(index);
    return sequence.getArrivalTimeForIndex(_offset);
  }

  public int getDepartureTimeForIndex(int index) {
    List<BlockSequence> sequences = _index.getSequences();
    BlockSequence sequence = sequences.get(index);
    return sequence.getDepartureTimeForIndex(_offset);
  }

  /****
   * Private Methods
   ****/

  private ServiceInterval computeServiceInterval(BlockSequenceIndex index,
      int stopIndex) {
    List<BlockSequence> sequences = _index.getSequences();
    BlockStopTimeEntry fromBst = sequences.get(0).getStopTimes().get(stopIndex);
    BlockStopTimeEntry toBst = sequences.get(sequences.size() - 1).getStopTimes().get(
        stopIndex);

    StopTimeEntry fromSt = fromBst.getStopTime();
    StopTimeEntry toSt = toBst.getStopTime();

    return new ServiceInterval(fromSt.getArrivalTime(),
        fromSt.getDepartureTime(), toSt.getArrivalTime(),
        toSt.getDepartureTime());
  }

  private class ListImpl extends AbstractList<BlockStopTimeEntry> {

    @Override
    public BlockStopTimeEntry get(int index) {
      List<BlockSequence> sequences = _index.getSequences();
      BlockSequence sequence = sequences.get(index);
      return sequence.getStopTimes().get(_offset);
    }

    @Override
    public int size() {
      return _index.size();
    }
  }

}
