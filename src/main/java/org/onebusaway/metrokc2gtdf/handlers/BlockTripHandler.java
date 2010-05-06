/**
 * 
 */
package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.metrokc2gtdf.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtdf.model.MetroKCTrip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockTripHandler extends InputHandler {

  private static String[] BLOCK_TRIPS_FIELDS = {
      "changeDate", "block_id", "trip_id", "trip_sequence", "dbModDate",
      "tripEndTime", "tripStartTime"};

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

  public int getBlockForTrip(int tripId) {
    MetroKCBlockTrip block = _tripIdToBlock.get(tripId);
    if (block == null)
      throw new IllegalStateException("no block for trip");
    return block.getBlockId();
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
}