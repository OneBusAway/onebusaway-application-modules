/**
 * 
 */
package org.onebusaway.metrokc2gtfs.handlers;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.metrokc2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockTripHandler extends InputHandler {

  private static BlockTripComparator _blockTripComparator = new BlockTripComparator();

  private static String[] BLOCK_TRIPS_FIELDS = {
      "changeDate", "block_id", "trip_id", "trip_sequence", "dbModDate", "tripEndTime", "tripStartTime"};

  private Map<Integer, MetroKCBlockTrip> _tripIdToBlock = new HashMap<Integer, MetroKCBlockTrip>();

  private Map<Integer, List<MetroKCBlockTrip>> _blockIdToBlocks = new FactoryMap<Integer, List<MetroKCBlockTrip>>(
      new ArrayList<MetroKCBlockTrip>());

  public BlockTripHandler() {
    super(MetroKCBlockTrip.class, BLOCK_TRIPS_FIELDS);
  }

  public void handleEntity(Object bean) {
    MetroKCBlockTrip block = (MetroKCBlockTrip) bean;
    if (_tripIdToBlock.containsKey(block.getTripId()))
      throw new IllegalStateException("trip appears in multiple blocks?");
    _tripIdToBlock.put(block.getTripId(), block);
    _blockIdToBlocks.get(block.getBlockId()).add(block);
  }

  public MetroKCBlockTrip getBlockForTrip(int tripId) {
    MetroKCBlockTrip block = _tripIdToBlock.get(tripId);
    if (block == null)
      throw new IllegalStateException("no block for trip");
    return block;
  }

  public List<MetroKCBlockTrip> getTripBlocksForTrips(List<MetroKCTrip> trips) {
    List<MetroKCBlockTrip> blocks = new ArrayList<MetroKCBlockTrip>();
    for (MetroKCTrip trip : trips)
      blocks.add(_tripIdToBlock.get(trip.getId()));
    return blocks;
  }

  public Map<Integer, List<MetroKCBlockTrip>> getBlockTripsByBlockIds(Set<Integer> ids) {
    Map<Integer, List<MetroKCBlockTrip>> blocks = new HashMap<Integer, List<MetroKCBlockTrip>>();
    for (Integer blockId : ids)
      blocks.put(blockId, _blockIdToBlocks.get(blockId));
    return blocks;
  }

  public Map<Integer, List<MetroKCBlockTrip>> getAllBlockTripsByBlockId() {
    return _blockIdToBlocks;
  }

  @Override
  public void close() {

    super.close();

    int hits = 0;

    for (Map.Entry<Integer, List<MetroKCBlockTrip>> entry : _blockIdToBlocks.entrySet()) {
      List<MetroKCBlockTrip> trips = entry.getValue();
      Collections.sort(trips, _blockTripComparator);

      for (int i = 0; i < trips.size(); i++) {
        MetroKCBlockTrip trip = trips.get(i);
        if (trip.getTripSequence() != i) {
          hits++;
          trip.setTripSequence(i);
        }
      }
    }

    System.err.println("out of order block trip sequene values=" + hits);
  }

  private static class BlockTripComparator implements Comparator<MetroKCBlockTrip> {

    public int compare(MetroKCBlockTrip o1, MetroKCBlockTrip o2) {
      return o1.getTripSequence() - o2.getTripSequence();
    }
  }
}