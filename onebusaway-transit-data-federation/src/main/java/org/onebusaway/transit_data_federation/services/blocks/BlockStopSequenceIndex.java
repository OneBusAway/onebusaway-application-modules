/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

/**
 * A {@link BlockStopSequenceIndex} is a pointer into a
 * {@link BlockSequenceIndex} for a particular stop in the sequence. Given that
 * the underlying {@link BlockSequenceIndex} is an ordered index of
 * {@link BlockSequence} objects where each sequence covers a list of stops, the
 * block stop sequence index is a slice of the underlying index for just one
 * stop. It allows for quick lookup of {@link BlockStopTimeEntry} entries at
 * that stop for the underlying block sequences.
 * 
 * @author bdferris
 * 
 * @see BlockSequence
 * @see BlockSequenceIndex
 * @see BlockIndexService
 */
@TransitTimeIndex
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
  
  public BlockSequence getBlockSequenceForIndex(int index) {
    return _index.getSequences().get(index);
  }

  /****
   * {@link HasIndexedBlockStopTimes} Interface
   ****/

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    return _stopTimes;
  }

  @Override
  public int getArrivalTimeForIndex(int index) {
    List<BlockSequence> sequences = _index.getSequences();
    BlockSequence sequence = sequences.get(index);
    return sequence.getArrivalTimeForIndex(_offset);
  }

  @Override
  public int getDepartureTimeForIndex(int index) {
    List<BlockSequence> sequences = _index.getSequences();
    BlockSequence sequence = sequences.get(index);
    return sequence.getDepartureTimeForIndex(_offset);
  }

  @Override
  public String toString() {
    List<BlockSequence> sequences = _index.getSequences();
    BlockSequence sequence = sequences.get(0);
    List<BlockStopTimeEntry> stopTimes = sequence.getStopTimes();
    BlockStopTimeEntry bst = stopTimes.get(_offset);
    return bst.toString();
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
